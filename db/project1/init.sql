CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  apellido VARCHAR(255) NOT NULL,
  telefono VARCHAR(50) NOT NULL UNIQUE,
  contraseña VARCHAR(512) NOT NULL,
  fecha_registro TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS pets (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  id_reporte BIGINT REFERENCES reports(id) ON DELETE SET NULL,
  nombre VARCHAR(255) NOT NULL,
  tipo VARCHAR(255) NOT NULL,
  estado VARCHAR(255) NOT NULL,
  descripcion VARCHAR(1000) NOT NULL
);

CREATE TABLE IF NOT EXISTS reports (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  pet_id BIGINT REFERENCES pets(id) ON DELETE SET NULL,
  tipo_reporte VARCHAR(255) NOT NULL,
  estado_reporte VARCHAR(255) NOT NULL,
  fecha_evento TIMESTAMPTZ NOT NULL,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
);

CREATE TABLE IF NOT EXISTS imagen (
  id_imagen BIGSERIAL PRIMARY KEY,
  id_reporte BIGINT REFERENCES reports(id) ON DELETE CASCADE,
  imagen_url VARCHAR(700),
  thumbnail_url VARCHAR(700),
  creado_en TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ubicacion (
  id_ubicacion BIGSERIAL PRIMARY KEY,
  id_reporte BIGINT REFERENCES reports(id) ON DELETE CASCADE,
  lugar_desaparicion VARCHAR(300),
  latitud NUMERIC(12,9),
  longitud NUMERIC(12,9),
  geom geometry(Point,4326)
);

-- Opcional: disparador para poblar geom a partir de lat/long
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

-- Índices
CREATE INDEX IF NOT EXISTS idx_reports_user_id ON reports(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_pet_id ON reports(pet_id);
CREATE INDEX IF NOT EXISTS idx_imagen_reporte ON imagen(id_reporte);
CREATE INDEX IF NOT EXISTS idx_ubicacion_reporte ON ubicacion(id_reporte);
CREATE INDEX IF NOT EXISTS idx_ubicacion_geom ON ubicacion USING GIST (geom);
