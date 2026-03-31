package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.dto.PetCreateDto;
import com.redpatitas.redPatitas.dto.ReportCreateDto;
import com.redpatitas.redPatitas.dto.ReportResponseDto;
import com.redpatitas.redPatitas.dto.request.ReportFrontendRequestDto;
import com.redpatitas.redPatitas.mapper.PetMapper;
import com.redpatitas.redPatitas.mapper.ReportMapper;
import com.redpatitas.redPatitas.repository.PetRepository;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.repository.UserRepository;
import com.redpatitas.redPatitas.service.interfaces.FileStorageService;
import com.redpatitas.redPatitas.service.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final ReportMapper reportMapper;
    private final PetMapper petMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Async
    public CompletableFuture<ReportResponseDto> create(ReportCreateDto dto, MultipartFile image) {
        var user = dto.userId() != null ? userRepository.findById(dto.userId()).orElse(null) : null;
        var pet = dto.petId() != null ? petRepository.findById(dto.petId()).orElse(null) : null;

        var report = reportMapper.toEntity(dto, user, pet);

        if (image != null && !image.isEmpty()) {
            try {
                var upload = fileStorageService.uploadImageAndThumbnail(image);
                report.setImagenUrl(upload.originalUrl());
                report.setThumbnailUrl(upload.thumbnailUrl());
            } catch (IOException e) {
                throw new RuntimeException("Error al subir imagen del reporte", e);
            }
        }

        var saved = reportRepository.save(report);
        return CompletableFuture.completedFuture(reportMapper.toDto(saved));
    }

    @Override
    @Async
    public CompletableFuture<ReportResponseDto> createFromFrontendForm(ReportFrontendRequestDto dto, MultipartFile image) {
        String tipoPet = "otro".equalsIgnoreCase(dto.tipo()) && dto.tipoOtro() != null && !dto.tipoOtro().isBlank()
                ? dto.tipoOtro()
                : dto.tipo();
        String nombrePet = dto.nombre() != null && !dto.nombre().isBlank() ? dto.nombre() : "Sin nombre";

        var pet = petRepository.save(petMapper.toEntity(new PetCreateDto(
            nombrePet,
                tipoPet,
                dto.estado(),
                dto.descripcion()
        )));

        var createDto = reportMapper.fromFrontendDto(dto, pet.getId());
        return create(createDto, image);
    }

    @Override
    @Async
    public CompletableFuture<List<ReportResponseDto>> findAll() {
        var reports = reportRepository.findAll().stream().map(reportMapper::toDto).toList();
        return CompletableFuture.completedFuture(reports);
    }
}
