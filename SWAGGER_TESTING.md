## Ejemplos para Probar Backend ReddePatitas en Swagger

### 1. Crear Reporte desde Formulario Frontend (CON IMAGEN)

**Endpoint:** `POST /api/reports/form`

**Descripción:** Este es el endpoint principal que tu frontend usará. Recibe multipart/form-data con:
- `payload`: JSON con los datos del reporte y mascota
- `image`: Archivo de imagen (opcional pero recomendado)

> Importante: `payload` debe enviarse como `application/json` dentro del multipart.

#### JSON del Payload (copiar y pegar en Swagger):

```json
{
  "userId": 1,
  "estado": "perdido",
  "tipo": "gato",
  "tipo_otro": "",
  "nombre": "Venus",
  "descripcion": "Gato blanco de pelaje frondoso",
  "fecha_desaparicion": "2026-03-29T05:00:00Z",
  "lugar_desaparicion": "Vereda la Cascada, Caramanta",
  "latitud": 5.528010967426297,
  "longitud": -75.65569253490617,
  "creadoEn": "2026-03-29T16:04:40.218Z"
}
```

#### Pasos en Swagger:
1. Abre http://localhost:8080/swagger-ui.html
2. Busca "Reports" → "POST /api/reports/form"
3. Haz clic en "Try it out"
4. En el campo "payload", pega el JSON anterior
5. En el campo "image", selecciona una imagen (JPG o PNG)
6. Haz clic en "Execute"

---

### 2. Crear Usuario

**Endpoint:** `POST /api/users`

#### JSON:

```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "telefono": "3101234567",
  "contrasena": "MiSeguraContraseña123"
}
```

---

### 3. Crear Mascota (manual)

**Endpoint:** `POST /api/pets`

#### JSON:

```json
{
  "nombre": "Max",
  "tipo": "perro",
  "estado": "activo",
  "descripcion": "Perro labrador retriever de color dorado"
}
```

---

### 4. Listar Todos los Reportes

**Endpoint:** `GET /api/reports`

No necesita payload, solo haz clic en "Execute"

---

### 5. Verificar que el Backend está Activo

**Endpoint:** `GET /api/health`

Debería retornar:
```json
{
  "status": "UP",
  "message": "Backend ReddePatitas funcionando correctamente"
}
```

---

## Ejemplos con cURL (línea de comandos)

### Crear Reporte con Imagen:

```bash
# Primero, descarga una imagen o usa una que tengas
curl -X POST http://localhost:8080/api/reports/form \
  -F 'payload={
    "userId": 1,
    "estado": "perdido",
    "tipo": "gato",
    "tipo_otro": "",
    "nombre": "Venus",
    "descripcion": "Gato blanco de pelaje frondoso",
    "fecha_desaparicion": "2026-03-29T05:00:00Z",
    "lugar_desaparicion": "Vereda la Cascada, Caramanta",
    "latitud": 5.528010967426297,
    "longitud": -75.65569253490617,
    "creadoEn": "2026-03-29T16:04:40.218Z"
  };type=application/json' \
  -F 'image=@/ruta/a/tu/imagen.jpg'
```

### Crear Reporte sin Imagen:

```bash
curl -X POST http://localhost:8080/api/reports/form \
  -F 'payload={
    "userId": 1,
    "estado": "perdido",
    "tipo": "gato",
    "tipo_otro": "",
    "nombre": "Venus",
    "descripcion": "Gato blanco de pelaje frondoso",
    "fecha_desaparicion": "2026-03-29T05:00:00Z",
    "lugar_desaparicion": "Vereda la Cascada, Caramanta",
    "latitud": 5.528010967426297,
    "longitud": -75.65569253490617,
    "creadoEn": "2026-03-29T16:04:40.218Z"
  };type=application/json'
```

### Crear Usuario:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "telefono": "3101234567",
    "contrasena": "MiSeguraContraseña123"
  }'
```

### Listar reportes:

```bash
curl http://localhost:8080/api/reports
```

---

## Respuesta Esperada (Reporte Creado)

```json
{
  "id": 1,
  "tipoReporte": "perdida",
  "estadoReporte": "perdido",
  "fechaEvento": "2026-03-29T05:00:00Z",
  "fechaCreacion": "2026-03-30T12:34:56.123Z",
  "lugarDesaparicion": "Vereda la Cascada, Caramanta",
  "latitud": 5.528010967426297,
  "longitud": -75.65569253490617,
  "imagenUrl": "https://...blob-url-azure...",
  "thumbnailUrl": "https://...blob-url-azure...",
  "userId": 1,
  "petId": 1
}
```

---

## Notas Importantes

1. **Si no tienes un usuario:** El endpoint `/api/reports/form` puede crear reportes sin userId (se guardará como null)
2. **La imagen es opcional:** Si no envías imagen, no se procesará con FileStorageService
3. **El endpoint maneja asincronía:** La respuesta puede tardar un poco por el procesamiento y guardado de imagen
4. **La mascota se crea automáticamente:** No necesitas crearla por separado si usas `/form`
5. **Latitud y Longitud:** En payload y respuesta se manejan como decimales

---

## Pasos para Iniciar y Probar:

```bash
# 1. Levanta la base local con Docker Compose
docker compose up -d

# 2. Inicia la app:
mvn spring-boot:run

# 3. Espera a que veas "Started redPatitas in X seconds"
# 4. Abre en navegador:
http://localhost:8080/swagger-ui.html

# 5. ¡Empieza a probar!
```

---

## Troubleshooting

- **Error 400 en Swagger:** Verifica que el JSON sea válido (sin comillas adicionales)
- **Error en `payload` multipart:** Verifica que la parte `payload` se envíe como `application/json`
- **Error al subir imagen en modo local:** Verifica que exista la carpeta `local-uploads` y que el contenedor `nginx_static` esté activo
- **Error al subir imagen en modo azure:** Revisa `storage.mode=azure` y las propiedades `azure.storage.*` en application.properties
- **Timeout:** Es normal que tarde 2-3 segundos por la subida de imagen asíncrona
- **ConnectionString inválida:** Revisa `azure.storage.connection-string` en application.properties

