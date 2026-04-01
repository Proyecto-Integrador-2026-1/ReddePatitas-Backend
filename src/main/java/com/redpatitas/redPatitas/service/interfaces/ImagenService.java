package com.redpatitas.redPatitas.service.interfaces;

public interface ImagenService {
    void saveForReport(Long reportId, String imagenUrl, String thumbnailUrl);
    void saveForReport(Long reportId, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;
}
