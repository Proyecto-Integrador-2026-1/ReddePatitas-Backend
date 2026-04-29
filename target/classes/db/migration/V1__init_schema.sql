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
  estado VARCHAR(255) NOT NULL DEFAULT 'ACTIVO'
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

-- Tabla conversation
CREATE TABLE IF NOT EXISTS conversation (
  conversacion_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_report UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
  owner_id UUID NOT NULL,
  user_id2 UUID NOT NULL,
  creado_en TIMESTAMPTZ DEFAULT now(),
  CONSTRAINT uq_conversation_report_owner_user2 UNIQUE (id_report, owner_id, user_id2)
);

CREATE INDEX IF NOT EXISTS idx_conversation_report_id ON conversation(id_report);
CREATE INDEX IF NOT EXISTS idx_conversation_owner_id ON conversation(owner_id);
CREATE INDEX IF NOT EXISTS idx_conversation_user_id2 ON conversation(user_id2);

-- Tabla message
CREATE TABLE IF NOT EXISTS message (
  mensaje_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  conversacion_id UUID NOT NULL REFERENCES conversation(conversacion_id) ON DELETE CASCADE,
  id_remitente UUID NOT NULL,
  contenido VARCHAR(1000),
  estado VARCHAR(255),
  creado_en TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_message_conversacion_id ON message(conversacion_id);
CREATE INDEX IF NOT EXISTS idx_message_remitente_id ON message(id_remitente);

-- Índice parcial para acelerar conteo de mensajes no leídos por conversación
CREATE INDEX IF NOT EXISTS idx_message_unread ON message(conversacion_id, estado) WHERE estado = 'NO_LEIDO';