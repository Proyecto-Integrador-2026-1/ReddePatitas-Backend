package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    // Tamaño de miniatura
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;
    private static final Path UPLOAD_ROOT = Paths.get("local-uploads");
    private static final Path ORIGINALS_DIR = UPLOAD_ROOT.resolve("originals");
    private static final Path THUMBNAILS_DIR = UPLOAD_ROOT.resolve("thumbnails");

    @Override
    public FileStorageService.UploadResult uploadImageAndThumbnail(MultipartFile file) throws IOException {
        ensureDirectories();

        // Validar que sea una imagen
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        // Generar nombre único
        String safeOriginalName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        Path originalPath = ORIGINALS_DIR.resolve(safeOriginalName);

        // Guardar imagen original
        Files.write(originalPath, file.getBytes());
        String originalUrl = originalPath.toUri().toString();

        // Generar miniatura en memoria
        byte[] thumbnailBytes = generateThumbnail(file.getBytes());

        // Guardar miniatura
        String thumbnailName = UUID.randomUUID() + "_thumb.jpg";
        Path thumbnailPath = THUMBNAILS_DIR.resolve(thumbnailName);
        Files.write(thumbnailPath, thumbnailBytes);
        String thumbnailUrl = thumbnailPath.toUri().toString();

        return new FileStorageService.UploadResult(originalUrl, thumbnailUrl);
    }

    private byte[] generateThumbnail(byte[] imageBytes) throws IOException {
        // Leer imagen original
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IOException("No se pudo leer la imagen");
        }

        // Calcular dimensiones manteniendo proporción
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double ratio = Math.min(
                (double) THUMBNAIL_WIDTH / originalWidth,
                (double) THUMBNAIL_HEIGHT / originalHeight
        );
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // Crear imagen redimensionada
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        // Si es necesario, recortar a cuadrado (opcional, para que todas las miniaturas sean del mismo tamaño)
        // Aquí optamos por no recortar para no perder información.

        // Convertir a JPEG (podrías mantener el formato original si lo prefieres)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", baos);
        return baos.toByteArray();
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        ensureDirectories();
        String safeOriginalName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        Path originalPath = ORIGINALS_DIR.resolve(safeOriginalName);
        Files.write(originalPath, file.getBytes());
        return originalPath.toUri().toString();
    }

    @Override
    public String uploadThumbnail(byte[] thumbnailBytes, String contentType) throws IOException {
        ensureDirectories();
        String fileExtension = contentType != null && contentType.contains("png") ? "png" : "jpg";
        String thumbnailName = UUID.randomUUID() + "_thumb." + fileExtension;
        Path thumbnailPath = THUMBNAILS_DIR.resolve(thumbnailName);
        Files.write(thumbnailPath, thumbnailBytes);
        return thumbnailPath.toUri().toString();
    }

    private void ensureDirectories() throws IOException {
        Files.createDirectories(ORIGINALS_DIR);
        Files.createDirectories(THUMBNAILS_DIR);
    }

    private String sanitizeFilename(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "image";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}