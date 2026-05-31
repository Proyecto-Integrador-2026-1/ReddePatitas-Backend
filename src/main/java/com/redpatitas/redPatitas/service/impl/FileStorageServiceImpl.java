package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    // Configuración
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> SUPPORTED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp", "image/bmp"
    );
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "webp", "bmp"
    );

    private static final Path UPLOAD_ROOT = Paths.get("local-uploads");
    private static final Path ORIGINALS_DIR = UPLOAD_ROOT.resolve("originals");
    private static final Path THUMBNAILS_DIR = UPLOAD_ROOT.resolve("thumbnails");

    private final String publicBaseUrl;

    public FileStorageServiceImpl(@Value("${storage.public-url:http://localhost:8081/uploads}") String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    }

    @Override
    public UploadResult uploadImageAndThumbnail(MultipartFile file) throws IOException {
        // Validaciones previas
        validateFile(file);

        ensureDirectories();

        // Generar nombre único
        String safeOriginalName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        Path originalPath = ORIGINALS_DIR.resolve(safeOriginalName);

        // Leer bytes del archivo
        byte[] fileBytes = file.getBytes();

        // Verificar que el archivo no esté vacío
        if (fileBytes.length == 0) {
            throw new IOException("El archivo está vacío");
        }

        // Verificar tamaño máximo
        if (fileBytes.length > MAX_FILE_SIZE_BYTES) {
            throw new IOException("El archivo excede el tamaño máximo permitido de " + (MAX_FILE_SIZE_BYTES / (1024 * 1024)) + " MB");
        }

        // Guardar imagen original
        Files.write(originalPath, fileBytes);
        String originalUrl = publicBaseUrl + "/originals/" + safeOriginalName;

        // Generar miniatura
        byte[] thumbnailBytes;
        try {
            thumbnailBytes = generateThumbnail(fileBytes);
        } catch (IOException e) {
            log.error("Error generando miniatura para archivo: {}", safeOriginalName, e);
            // Si falla la generación de miniatura, usar la original como fallback
            thumbnailBytes = fileBytes;
        }

        // Guardar miniatura
        String thumbnailName = UUID.randomUUID() + "_thumb.jpg";
        Path thumbnailPath = THUMBNAILS_DIR.resolve(thumbnailName);
        Files.write(thumbnailPath, thumbnailBytes);
        String thumbnailUrl = publicBaseUrl + "/thumbnails/" + thumbnailName;

        log.info("Imagen subida exitosamente: original={}, thumbnail={}", originalUrl, thumbnailUrl);
        return new UploadResult(originalUrl, thumbnailUrl);
    }

    /**
     * Valida que el archivo sea una imagen válida
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null) {
            throw new IOException("No se proporcionó ningún archivo");
        }

        if (file.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        // Validar por content type
        if (contentType == null || !SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException("Formato de imagen no soportado. Formatos permitidos: JPEG, PNG, WEBP, BMP");
        }

        // Validar por extensión del archivo
        if (originalFilename != null && !originalFilename.isBlank()) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!SUPPORTED_EXTENSIONS.contains(extension)) {
                throw new IOException("Extensión de archivo no soportada. Extensiones permitidas: " + String.join(", ", SUPPORTED_EXTENSIONS));
            }
        }

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            long sizeMB = file.getSize() / (1024 * 1024);
            long maxMB = MAX_FILE_SIZE_BYTES / (1024 * 1024);
            throw new IOException("El archivo excede el tamaño máximo permitido de " + maxMB + " MB. Tamaño actual: " + sizeMB + " MB");
        }

        // Validar que sea una imagen legible (prueba de integridad)
        try {
            byte[] bytes = file.getBytes();
            BufferedImage testImage = ImageIO.read(new ByteArrayInputStream(bytes));
            if (testImage == null) {
                throw new IOException("El archivo no es una imagen válida o está corrupto");
            }
        } catch (Exception e) {
            throw new IOException("No se pudo leer la imagen. El archivo puede estar corrupto o en un formato no soportado: " + e.getMessage());
        }
    }

    /**
     * Obtiene la extensión de un nombre de archivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) return "";
        return filename.substring(lastDot + 1);
    }

    /**
     * Genera miniatura a partir de bytes de imagen
     */
    private byte[] generateThumbnail(byte[] imageBytes) throws IOException {
        // Leer imagen original
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IOException("No se pudo leer la imagen para generar miniatura");
        }

        // Calcular dimensiones manteniendo proporción
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= 0 || originalHeight <= 0) {
            throw new IOException("Dimensiones de imagen inválidas: " + originalWidth + "x" + originalHeight);
        }

        double ratio = Math.min(
                (double) THUMBNAIL_WIDTH / originalWidth,
                (double) THUMBNAIL_HEIGHT / originalHeight
        );
        int newWidth = Math.max(1, (int) (originalWidth * ratio));
        int newHeight = Math.max(1, (int) (originalHeight * ratio));

        // Crear imagen redimensionada
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        // Convertir a JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean success = ImageIO.write(thumbnail, "jpg", baos);
        if (!success) {
            throw new IOException("No se pudo codificar la miniatura a formato JPEG");
        }

        return baos.toByteArray();
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        validateFile(file);
        ensureDirectories();
        String safeOriginalName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        Path originalPath = ORIGINALS_DIR.resolve(safeOriginalName);
        Files.write(originalPath, file.getBytes());
        return publicBaseUrl + "/originals/" + safeOriginalName;
    }

    @Override
    public String uploadThumbnail(byte[] thumbnailBytes, String contentType) throws IOException {
        ensureDirectories();
        String fileExtension = contentType != null && contentType.contains("png") ? "png" : "jpg";
        String thumbnailName = UUID.randomUUID() + "_thumb." + fileExtension;
        Path thumbnailPath = THUMBNAILS_DIR.resolve(thumbnailName);
        Files.write(thumbnailPath, thumbnailBytes);
        return publicBaseUrl + "/thumbnails/" + thumbnailName;
    }

    private void ensureDirectories() throws IOException {
        Files.createDirectories(ORIGINALS_DIR);
        Files.createDirectories(THUMBNAILS_DIR);
    }

    private String sanitizeFilename(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "image";
        }
        // Remover caracteres peligrosos y espacios
        String name = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        // Limitar longitud
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        return name;
    }
}