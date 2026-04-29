package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.dto.request.ReportPublicationRequestDto;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.entity.ReportPublication;
import com.redpatitas.redPatitas.repository.ReportPublicationRepository;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.service.interfaces.ReportPublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportPublicationServiceImpl implements ReportPublicationService {

    private final ReportPublicationRepository reportPublicationRepository;
    private final ReportRepository reportRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public String submitReport(ReportPublicationRequestDto dto, UUID reporterUserId) {
        if (dto.getReportId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reportId requerido");

        Report report = reportRepository.findById(dto.getReportId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));

        var existing = reportPublicationRepository.findByReportIdAndUserId(dto.getReportId(), reporterUserId);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya reportaste esta publicación");
        }

        // Guardar `razon` por separado y `descripcion` solo como detalle opcional (evitar duplicación)
        String detalle = dto.getDescripcion() != null ? dto.getDescripcion() : "";

        ReportPublication rp = ReportPublication.builder()
            .report(report)
            .userId(reporterUserId)
            .razon(dto.getRazon() != null && !dto.getRazon().isBlank() ? dto.getRazon() : "otro")
            .descripcion(detalle)
            .build();

        try {
            reportPublicationRepository.save(rp);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya reportaste esta publicación");
        }

        // Calcular conteo y actualizar estado del reporte según umbral
        long count = reportPublicationRepository.countByReportId(dto.getReportId());

        try {
            String current = report.getEstado() != null ? report.getEstado() : "ACTIVO";
            if (count >= 5) {
                if (!"EN_REVISION".equalsIgnoreCase(current)) {
                    report.setEstado("EN_REVISION");
                    reportRepository.save(report);
                }
            } else {
                // Si aún no está en revisión y está en estado por defecto, marcar como reportada
                if (!"EN_REVISION".equalsIgnoreCase(current) && ("ACTIVO".equalsIgnoreCase(current) || "".equals(current))) {
                    report.setEstado("REPORTADA");
                    reportRepository.save(report);
                }
            }
        } catch (Exception ignored) {
        }

        // publish lightweight event for admin dashboard listeners (other services can listen)
        try {
            eventPublisher.publishEvent(Map.of(
                    "type", "REPORT_PUBLICATION",
                    "reportId", report.getId(),
                    "reportPublicationId", rp.getId(),
                    "reportedBy", reporterUserId,
                    "reason", dto.getRazon(),
                    "count", count
            ));
        } catch (Exception ignored) {
        }

        if (count >= 5) {
            try {
                eventPublisher.publishEvent(Map.of(
                        "type", "REPORT_PUBLICATION_THRESHOLD",
                        "reportId", report.getId(),
                        "count", count
                ));
            } catch (Exception ignored) {
            }
            return "Reporte realizado. Publicación pasa a revisión administrativa";
        }

        return "Reporte realizado";
    }
}
