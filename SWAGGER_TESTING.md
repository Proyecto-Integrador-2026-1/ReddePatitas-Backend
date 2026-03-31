## Ejemplos para Probar Backend ReddePatitas en Swagger

### 1. Crear Reporte desde Formulario Frontend (CON IMAGEN)

**Endpoint:** `POST /api/reports/form`

**Descripción:** Este es el endpoint principal que tu frontend usará. Recibe:
- `payload`: JSON con los datos del reporte y mascota
- `image`: Archivo de imagen (opcional pero recomendado)

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
  "latitud": "5.528010967426297",
  "longitud": "-75.65569253490617",
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
  }' \
  -F 'image=@/ruta/a/tu/imagen.jpg'
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
3. **El endpoint maneja asincronia:** La respuesta puede tardar un poco por la subida a Azure Blob Storage
4. **La mascota se crea automáticamente:** No necesitas crearla por separado si usas `/form`
5. **Latitud y Longitud:** Son decimales (no strings) en la respuesta

---

## Pasos para Iniciar y Probar:

```bash
# 1. Asegúrate de que PostgreSQL está corriendo (Supabase en application.properties)
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
- **Error al subir imagen:** Asegúrate que tu Azure Blob Storage esté configurado en application.properties
- **Timeout:** Es normal que tarde 2-3 segundos por la subida de imagen asíncrona
- **ConnectionString inválida:** Revisa `azure.storage.connection-string` en application.properties

