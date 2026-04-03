package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.entity.Imagen;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.repository.ImagenRepository;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.service.interfaces.ImagenService;
import com.redpatitas.redPatitas.service.interfaces.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ImagenServiceImpl implements ImagenService {

    private final ImagenRepository imagenRepository;
    private final ReportRepository reportRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public void saveForReport(Long reportId, String imagenUrl, String thumbnailUrl) {
        if (imagenUrl == null && thumbnailUrl == null) return;
        Report report = reportRepository.getReferenceById(reportId);
        Imagen imagen = Imagen.builder()
                .report(report)
                .imagenUrl(imagenUrl)
                .thumbnailUrl(thumbnailUrl)
                .creadoEn(Instant.now())
                .build();
        imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public void saveForReport(Long reportId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return;
        var upload = fileStorageService.uploadImageAndThumbnail(file);
        saveForReport(reportId, upload.originalUrl(), upload.thumbnailUrl());
    }
}
