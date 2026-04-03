# ReddePatitas-Backend
Backend de Red de Patitas, construido con Spring Boot, JPA, PostgreSQL/PostGIS, Flyway y almacenamiento local de imágenes.

## Estructura general

- `controller/`: endpoints REST
- `service/`: lógica de negocio e ինտերfaz
- `repository/`: acceso a datos con Spring Data JPA
- `entity/`: entidades JPA
- `mapper/`: conversión entre entidades y DTOs
- `dto/request/`: objetos de entrada
- `dto/response/`: objetos de salida
- `config/`: propiedades y configuración técnica
- `exception/`: manejo global de errores

## Arranque local

1. Levantar la base de datos y el contenedor de archivos estáticos:
	`docker compose up -d`
2. Iniciar la aplicación:
	`./mvnw spring-boot:run`
3. Al arrancar, Flyway crea el esquema en PostgreSQL usando las migraciones de `src/main/resources/db/migration`.

## Notas de uso

- Los reportes se crean mediante `POST /api/reports/form` con multipart/form-data: `payload` JSON + `image` opcional.
- Las mascotas, usuarios y reportes tienen DTOs separados en `request` y `response`.
- El almacenamiento local publica imágenes desde `http://localhost:8081/uploads`.
