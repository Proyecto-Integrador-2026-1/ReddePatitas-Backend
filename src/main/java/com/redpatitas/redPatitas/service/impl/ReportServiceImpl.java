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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final ReportMapper reportMapper;
    private final PetMapper petMapper;
    private final com.redpatitas.redPatitas.service.interfaces.ImagenService imagenService;
    private final com.redpatitas.redPatitas.service.interfaces.UbicacionService ubicacionService;
    private final com.redpatitas.redPatitas.repository.ImagenRepository imagenRepository;
    private final com.redpatitas.redPatitas.repository.UbicacionRepository ubicacionRepository;

    @Override
    @Async
    public CompletableFuture<ReportResponseDto> create(ReportCreateDto dto, MultipartFile image) {
        var user = dto.userId() != null ? userRepository.findById(dto.userId()).orElse(null) : null;
        var pet = dto.petId() != null ? petRepository.findById(dto.petId()).orElse(null) : null;

        var report = reportMapper.toEntity(dto, user, pet);

        // La gestión del archivo (subida y persistencia) se delega a ImagenService

        var saved = reportRepository.save(report);

        // Guardar imagen usando servicio (ImagenService maneja la subida)
        try {
            if (image != null && !image.isEmpty()) {
                imagenService.saveForReport(saved.getId(), image);
            }
        } catch (Exception e) {
            log.warn("No se pudo insertar fila en tabla imagen via servicio: {}", e.getMessage());
        }

        // Guardar ubicacion usando servicio
        try {
            if (dto != null) {
                var lugar = dto.lugarDesaparicion();
                var lat = dto.latitud();
                var lon = dto.longitud();
                if (lugar != null || lat != null || lon != null) {
                    ubicacionService.saveForReport(saved.getId(), lugar, lat, lon);
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo insertar fila en tabla ubicacion via servicio: {}", e.getMessage());
        }

        var imagenOpt = imagenRepository.findLatestByReportId(saved.getId());
        var ubicacionOpt = ubicacionRepository.findLatestByReportId(saved.getId());
        log.debug("Saved report id={} imagenPresent={} ubicacionPresent={}", saved.getId(), imagenOpt.isPresent(), ubicacionOpt.isPresent());
        return CompletableFuture.completedFuture(reportMapper.toDto(saved, imagenOpt.orElse(null), ubicacionOpt.orElse(null)));
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
        var reports = reportRepository.findAll().stream().map(r -> {
            var img = imagenRepository.findLatestByReportId(r.getId()).orElse(null);
            var ub = ubicacionRepository.findLatestByReportId(r.getId()).orElse(null);
            return reportMapper.toDto(r, img, ub);
        }).toList();
        return CompletableFuture.completedFuture(reports);
    }
}
