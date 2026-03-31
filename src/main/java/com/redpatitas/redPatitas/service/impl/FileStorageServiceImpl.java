package com.redpatitas.redPatitas.service.impl;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.redpatitas.redPatitas.service.interfaces.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Qualifier("blobContainerClient")
    private final BlobContainerClient blobContainerClient;

    @Qualifier("thumbnailContainerClient")
    private final BlobContainerClient thumbnailContainerClient;

    // Tamaño de miniatura
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    @Override
    public FileStorageService.UploadResult uploadImageAndThumbnail(MultipartFile file) throws IOException {
        // Validar que sea una imagen
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        // Generar nombre único
        String originalBlobName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String thumbnailBlobName = UUID.randomUUID() + "_thumb_" + file.getOriginalFilename();

        // Subir imagen original
        var originalBlobClient = blobContainerClient.getBlobClient(originalBlobName);
        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
        originalBlobClient.upload(file.getInputStream(), file.getSize(), true);
        originalBlobClient.setHttpHeaders(headers);
        String originalUrl = originalBlobClient.getBlobUrl();

        // Generar miniatura en memoria
        byte[] thumbnailBytes = generateThumbnail(file.getBytes());

        // Subir miniatura
        var thumbnailBlobClient = thumbnailContainerClient.getBlobClient(thumbnailBlobName);
        thumbnailBlobClient.upload(new ByteArrayInputStream(thumbnailBytes), thumbnailBytes.length, true);
        thumbnailBlobClient.setHttpHeaders(new BlobHttpHeaders().setContentType("image/jpeg"));
        String thumbnailUrl = thumbnailBlobClient.getBlobUrl();

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
        // Si no quieres subir miniatura, solo imagen original
        String blobName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        var blobClient = blobContainerClient.getBlobClient(blobName);
        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(headers);
        return blobClient.getBlobUrl();
    }

    @Override
    public String uploadThumbnail(byte[] thumbnailBytes, String contentType) throws IOException {
        String blobName = UUID.randomUUID() + "_thumb.jpg";
        var blobClient = thumbnailContainerClient.getBlobClient(blobName);
        blobClient.upload(new ByteArrayInputStream(thumbnailBytes), thumbnailBytes.length, true);
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
        return blobClient.getBlobUrl();
    }
}