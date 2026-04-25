package com.redpatitas.redPatitas.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redpatitas.redPatitas.dto.request.ReportFormRequestDto;
import com.redpatitas.redPatitas.dto.response.ReportPrincipalResponseDto;
import com.redpatitas.redPatitas.dto.response.ReportResponseDto;
import com.redpatitas.redPatitas.entity.Imagen;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.entity.Ubicacion;
import com.redpatitas.redPatitas.mapper.ReportMapper;
import com.redpatitas.redPatitas.repository.ImagenRepository;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.repository.UbicacionRepository;
import com.redpatitas.redPatitas.service.interfaces.FileStorageService;  // ← importar
import com.redpatitas.redPatitas.service.interfaces.ReportService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ReportRepository reportRepository;
    private final ImagenRepository imagenRepository;
    private final UbicacionRepository ubicacionRepository;
    private final ReportMapper reportMapper;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService; // ahora sí resuelve

    @PersistenceContext
    private EntityManager entityManager;

    // Este es el método principal que viene de la interfaz
    @Override
    @Async
    public CompletableFuture<ReportResponseDto> createFromFrontendForm(ReportFormRequestDto dto, MultipartFile image) {
        try {
            // 1. Procesar la imagen (si viene)
            String imagenUrl = null;
            String thumbnailUrl = null;
            if (image != null && !image.isEmpty()) {
                var uploadResult = fileStorageService.uploadImageAndThumbnail(image);
                imagenUrl = uploadResult.originalUrl();
                thumbnailUrl = uploadResult.thumbnailUrl();
            }

            // 2. Llamar a la función de base de datos
            String json = objectMapper.writeValueAsString(dto);
            UUID reportId = (UUID) entityManager.createNativeQuery(
                    "SELECT crear_reporte_completo(?::jsonb)")
                    .setParameter(1, json)
                    .getSingleResult();

            // 3. Obtener el reporte
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("Reporte no encontrado después de creación"));

            // 4. Guardar imagen asociada
            if (imagenUrl != null) {
                Imagen imagen = Imagen.builder()
                        .report(report)
                        .imagenUrl(imagenUrl)
                        .thumbnailUrl(thumbnailUrl)
                        .creadoEn(Instant.now())
                        .build();
                imagenRepository.save(imagen);
            }

            // 5. Obtener ubicación
            Ubicacion ubicacion = ubicacionRepository.findByReportId(reportId).orElse(null);

            // 6. Construir respuesta
            Imagen imagenGuardada = imagenUrl != null ? imagenRepository.findLatestByReportId(reportId).orElse(null) : null;
            ReportResponseDto response = reportMapper.toDto(report, imagenGuardada, ubicacion);
            return CompletableFuture.completedFuture(response);

        } catch (Exception e) {
            log.error("Error al crear reporte", e);
            throw new RuntimeException("Error al crear reporte", e);
        }
    }

    @Override
    @Async
    public CompletableFuture<List<ReportResponseDto>> findAll() {
        List<ReportResponseDto> list = reportRepository.findAll().stream()
                .map(r -> {
                    Imagen img = imagenRepository.findLatestByReportId(r.getId()).orElse(null);
                    Ubicacion ub = ubicacionRepository.findByReportId(r.getId()).orElse(null);
                    return reportMapper.toDto(r, img, ub);
                })
                .toList();
        return CompletableFuture.completedFuture(list);
    }
    @Override
    @Async
    public CompletableFuture<List<ReportPrincipalResponseDto>> findAllForPrincipal() {
        var reports = reportRepository.findAllWithPet().stream()
                .map(report -> {
                    var pet = report.getPet();
                    var img = imagenRepository.findLatestByReportId(report.getId()).orElse(null);
                    var ub = ubicacionRepository.findByReportId(report.getId()).orElse(null);

                    String estado = pet != null && pet.getEstado() != null && !pet.getEstado().isBlank()
                            ? pet.getEstado()
                            : report.getTipoReporte();

                    return new ReportPrincipalResponseDto(
                            report.getId(),
                            pet != null && pet.getNombre() != null && !pet.getNombre().isBlank() ? pet.getNombre() : "Sin nombre",
                            estado != null && !estado.isBlank() ? estado : "perdido",
                            pet != null && pet.getTipo() != null && !pet.getTipo().isBlank() ? pet.getTipo() : "desconocido",
                            pet != null ? pet.getDescripcion() : null,
                            ub != null ? ub.getLugarDesaparicion() : null,
                            ub != null ? ub.getLatitud() : null,
                            ub != null ? ub.getLongitud() : null,
                            img != null ? img.getImagenUrl() : null,
                            img != null ? img.getThumbnailUrl() : null,
                            report.getFechaCreacion(),
                            report.getFechaEvento()
                    );
                })
                .filter(item -> item.latitud() != null && item.longitud() != null)
                .sorted(Comparator.comparing(ReportPrincipalResponseDto::fecha_publicacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return CompletableFuture.completedFuture(reports);
    }
}