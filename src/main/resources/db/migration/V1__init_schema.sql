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
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now()
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