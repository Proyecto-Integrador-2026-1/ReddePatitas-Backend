package com.redpatitas.redPatitas.service.interfaces;


import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    UploadResult uploadImageAndThumbnail(MultipartFile file) throws IOException;
    String uploadImage(MultipartFile file) throws IOException;
    String uploadThumbnail(byte[] thumbnailBytes, String contentType) throws IOException;

    record UploadResult(String originalUrl, String thumbnailUrl) {}
}
