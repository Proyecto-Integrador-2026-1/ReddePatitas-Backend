-- Extensiones
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- Tabla pets (sin FK a users)
CREATE TABLE IF NOT EXISTS pets (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  tipo VARCHAR(255) NOT NULL,
  estado VARCHAR(255) NOT NULL,
  descripcion VARCHAR(1000) NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- Tabla reports (sin FK a users, con FK a pets)
CREATE TABLE IF NOT EXISTS reports (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL,
  pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
  tipo_reporte VARCHAR(255) NOT NULL,
  fecha_evento TIMESTAMPTZ NOT NULL,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  estado VARCHAR(255) NOT NULL DEFAULT 'ACTIVO',
  reencontrado BOOLEAN NOT NULL DEFAULT false,
  -- Columna generada para mostrar explícitamente TRUE / FALSE en mayúsculas
  reencontrado_text VARCHAR(5) GENERATED ALWAYS AS (CASE WHEN reencontrado THEN 'TRUE' ELSE 'FALSE' END) STORED,
  mensaje_resolucion VARCHAR(1000),
  fecha_resuelta TIMESTAMPTZ,
  oculto BOOLEAN NOT NULL DEFAULT false,
  eliminado BOOLEAN NOT NULL DEFAULT false
);

-- Tabla imagen
CREATE TABLE IF NOT EXISTS imagen (
  id_imagen UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_reporte UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
  imagen_url VARCHAR(700),
  thumbnail_url VARCHAR(700),
  creado_en TIMESTAMPTZ DEFAULT now()
);

-- Tabla ubicacion
CREATE TABLE IF NOT EXISTS ubicacion (
  id_ubicacion UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_reporte UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
  lugar_desaparicion VARCHAR(300),
  latitud NUMERIC(12,9),
  longitud NUMERIC(12,9),
  geom geometry(Point,4326)
);

-- Función y trigger para geom
CREATE OR REPLACE FUNCTION ubicacion_set_geom()
RETURNS trigger AS $$
BEGIN
  IF NEW.latitud IS NOT NULL AND NEW.longitud IS NOT NULL THEN
    NEW.geom = ST_SetSRID(ST_MakePoint(NEW.longitud::double precision, NEW.latitud::double precision), 4326);
  ELSE
    NEW.geom = NULL;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_geom ON ubicacion;
CREATE TRIGGER trg_set_geom
BEFORE INSERT OR UPDATE ON ubicacion
FOR EACH ROW EXECUTE FUNCTION ubicacion_set_geom();

-- Función principal que procesa el JSON y crea mascota, reporte y ubicación
CREATE OR REPLACE FUNCTION crear_reporte_completo(data JSONB)
RETURNS UUID AS $$
DECLARE
  v_user_id UUID;
  v_estado VARCHAR;
  v_tipo VARCHAR;
  v_nombre VARCHAR;
  v_descripcion VARCHAR;
  v_fecha_evento TIMESTAMPTZ;
  v_lugar VARCHAR;
  v_lat NUMERIC;
  v_lon NUMERIC;
  v_creado_en TIMESTAMPTZ;
  v_pet_id UUID;
  v_report_id UUID;
BEGIN
  -- Extraer datos del JSON (coincide con ReportFormRequestDto)
  v_user_id := (data->>'userid')::UUID;
  v_estado := data->>'estado';
  v_tipo := data->>'tipo';
  v_nombre := data->>'nombre';
  v_descripcion := data->>'descripcion';
  v_fecha_evento := (data->>'fecha_desaparicion')::TIMESTAMPTZ;
  v_lugar := data->>'lugar_desaparicion';
  v_lat := (data->>'latitud')::NUMERIC;
  v_lon := (data->>'longitud')::NUMERIC;
  v_creado_en := (data->>'creadoEn')::TIMESTAMPTZ;

  -- Buscar mascota existente por user_id, nombre y tipo
  SELECT id INTO v_pet_id FROM pets
  WHERE user_id = v_user_id AND nombre = v_nombre AND tipo = v_tipo
  LIMIT 1;

  IF v_pet_id IS NULL THEN
    INSERT INTO pets (user_id, nombre, tipo, estado, descripcion)
    VALUES (v_user_id, v_nombre, v_tipo, v_estado, v_descripcion)
    RETURNING id INTO v_pet_id;
  ELSE
    -- Actualizar datos de la mascota por si cambia el estado o descripción
    UPDATE pets SET estado = v_estado, descripcion = v_descripcion
    WHERE id = v_pet_id;
  END IF;

  -- Insertar reporte
  INSERT INTO reports (user_id, pet_id, tipo_reporte, fecha_evento, fecha_creacion)
  VALUES (v_user_id, v_pet_id, v_estado, v_fecha_evento, COALESCE(v_creado_en, now()))
  RETURNING id INTO v_report_id;

  -- Insertar ubicación
  INSERT INTO ubicacion (id_reporte, lugar_desaparicion, latitud, longitud)
  VALUES (v_report_id, v_lugar, v_lat, v_lon);

  RETURN v_report_id;
END;
$$ LANGUAGE plpgsql;

-- Índices
CREATE INDEX IF NOT EXISTS idx_reports_user_id ON reports(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_pet_id ON reports(pet_id);
CREATE INDEX IF NOT EXISTS idx_pets_user_id ON pets(user_id);
CREATE INDEX IF NOT EXISTS idx_imagen_reporte ON imagen(id_reporte);
CREATE INDEX IF NOT EXISTS idx_ubicacion_reporte ON ubicacion(id_reporte);
CREATE INDEX IF NOT EXISTS idx_ubicacion_geom ON ubicacion USING GIST (geom);
-- Índices para nuevas columnas de resolución
CREATE INDEX IF NOT EXISTS idx_reports_reencontrado ON reports(reencontrado);
CREATE INDEX IF NOT EXISTS idx_reports_fecha_resuelta ON reports(fecha_resuelta);

-- Tabla report_publications (reportes de publicaciones)
CREATE TABLE IF NOT EXISTS report_publications (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
  user_id UUID NOT NULL,
  razon VARCHAR(100) NOT NULL,
  descripcion VARCHAR(1000),
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_report_user UNIQUE (report_id, user_id)
);

-- Función para crear un report_publication a partir de JSON (usa misma estructura de flujo — 'userid' en el payload)
CREATE OR REPLACE FUNCTION crear_reporte_publicacion(data JSONB)
RETURNS UUID AS $$
DECLARE
  v_user_id UUID;
  v_report_id UUID;
  v_razon VARCHAR;
  v_descripcion VARCHAR;
  v_rp_id UUID;
BEGIN
  v_user_id := (data->>'userid')::UUID;
  v_report_id := (data->>'reportId')::UUID;
  v_razon := COALESCE(data->>'razon', 'otro');
  v_descripcion := COALESCE(data->>'descripcion', '');

  -- Validar que el reporte exista
  IF NOT EXISTS (SELECT 1 FROM reports WHERE id = v_report_id) THEN
    RAISE EXCEPTION 'Reporte no encontrado: %', v_report_id;
  END IF;

  -- Si ya existe un reporte de publicación por el mismo usuario, devolverlo
  SELECT id INTO v_rp_id FROM report_publications
  WHERE report_id = v_report_id AND user_id = v_user_id
  LIMIT 1;

  IF v_rp_id IS NOT NULL THEN
    RETURN v_rp_id;
  END IF;

  -- Insertar nuevo report_publication
  INSERT INTO report_publications (report_id, user_id, razon, descripcion, fecha_creacion)
  VALUES (v_report_id, v_user_id, COALESCE(NULLIF(v_razon, ''), 'otro'), v_descripcion, COALESCE((data->>'createdAt')::timestamptz, now()))
  RETURNING id INTO v_rp_id;

  RETURN v_rp_id;
END;
$$ LANGUAGE plpgsql;

-- Índices para report_publications
CREATE INDEX IF NOT EXISTS idx_report_publications_report_id ON report_publications(report_id);
CREATE INDEX IF NOT EXISTS idx_report_publications_user_id ON report_publications(user_id);



-- 1. TABLA CONVERSACIÓN (compartida)
CREATE TABLE conversation (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_conversation_report_id ON conversation(report_id);

-- 2. TABLA MENSAJE
CREATE TABLE message (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'ENVIADO',
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_message_conversation_id ON message(conversation_id);
CREATE INDEX idx_message_sender_id ON message(sender_id);
CREATE INDEX idx_message_created_at ON message(created_at);

-- Índice parcial para mensajes NO_LEIDOS (optimización)
CREATE INDEX idx_message_unread ON message(conversation_id, status) 
WHERE status = 'NO_LEIDO';

-- 3. TABLA CONVERSACIÓN DE USUARIO (cada usuario tiene su copia)
CREATE TABLE user_conversation (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    other_user_id UUID NOT NULL,
    conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    report_id UUID NOT NULL,
    last_message TEXT,
    last_message_at TIMESTAMPTZ,
    unread_count INT DEFAULT 0,
    deleted_at TIMESTAMPTZ,          -- soft delete
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 📌 ÍNDICE ÚNICO PARCIAL
CREATE UNIQUE INDEX uq_user_conversation_active ON user_conversation (user_id, other_user_id, report_id) 
WHERE deleted_at IS NULL;

-- Índices adicionales para rendimiento
CREATE INDEX idx_user_conv_user_id ON user_conversation(user_id);
CREATE INDEX idx_user_conv_other_user ON user_conversation(other_user_id);
CREATE INDEX idx_user_conv_deleted ON user_conversation(deleted_at);
CREATE INDEX idx_user_conv_report ON user_conversation(report_id);
CREATE INDEX idx_user_conv_last_message ON user_conversation(last_message_at DESC);

-- 4. TRIGGER para mantener unread_count actualizado (opcional pero recomendado)
CREATE OR REPLACE FUNCTION update_unread_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE user_conversation uc
    SET unread_count = (
        SELECT COUNT(*) FROM message m
        WHERE m.conversation_id = NEW.conversation_id
          AND m.sender_id != uc.user_id
          AND m.status = 'NO_LEIDO'
    ),
    updated_at = NOW()
    WHERE uc.conversation_id = NEW.conversation_id AND uc.deleted_at IS NULL;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_unread_count
AFTER INSERT OR UPDATE OF status ON message
FOR EACH ROW
EXECUTE FUNCTION update_unread_count();

CREATE TABLE IF NOT EXISTS moderation_action (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  tipo_accion VARCHAR(50) NOT NULL,
  tipo_objetivo VARCHAR(50) NOT NULL,
  id_objetivo UUID NOT NULL,
  realizado_por UUID NOT NULL,
  motivo VARCHAR(1000),
  metadata JSONB,
  creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_moderation_action_target ON moderation_action(tipo_objetivo, id_objetivo);
