package com.redpatitas.redPatitas.mapper;

import com.redpatitas.redPatitas.dto.request.ReportCreateDto;
import com.redpatitas.redPatitas.dto.request.ReportFormRequestDto;
import com.redpatitas.redPatitas.dto.response.ReportResponseDto;
import com.redpatitas.redPatitas.entity.Pet;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class ReportMapper {

    public Report toEntity(ReportCreateDto dto, User user, Pet pet) {
        return Report.builder()
                .user(user)
                .pet(pet)
                .tipoReporte(dto.tipoReporte())
                .fechaEvento(dto.fechaEvento())
                .fechaCreacion(dto.fechaCreacion() != null ? dto.fechaCreacion() : Instant.now())
                .build();
    }

    public ReportCreateDto fromFrontendDto(ReportFormRequestDto dto, Long petId) {
        Instant fechaEvento = dto.fechaDesaparicion() != null
            ? dto.fechaDesaparicion().atStartOfDay().toInstant(ZoneOffset.UTC)
            : Instant.now();
        String tipoReporte = dto.estado() != null && !dto.estado().isBlank() ? dto.estado() : "perdida";
        return new ReportCreateDto(
                dto.userId(),
                petId,
                tipoReporte,
            fechaEvento,
                dto.creadoEn() != null ? dto.creadoEn() : Instant.now(),
                dto.lugarDesaparicion(),
                dto.latitud(),
                dto.longitud()
        );
    }

    public ReportResponseDto toDto(Report report) {
        return new ReportResponseDto(
                report.getId(),
                report.getTipoReporte(),
                report.getFechaEvento(),
                report.getFechaCreacion(),
                null,
                null,
                null,
                null,
                null,
                report.getUser() != null ? report.getUser().getId() : null,
                report.getPet() != null ? report.getPet().getId() : null
        );
    }

    public ReportResponseDto toDto(Report report, com.redpatitas.redPatitas.entity.Imagen imagen, com.redpatitas.redPatitas.entity.Ubicacion ubicacion) {
        java.math.BigDecimal lat = null;
        java.math.BigDecimal lon = null;
        if (ubicacion != null) {
            lat = ubicacion.getLatitud();
            lon = ubicacion.getLongitud();
        }
        String imagenUrl = imagen != null ? imagen.getImagenUrl() : null;
        String thumbnail = imagen != null ? imagen.getThumbnailUrl() : null;
        return new ReportResponseDto(
                report.getId(),
                report.getTipoReporte(),
                report.getFechaEvento(),
                report.getFechaCreacion(),
                ubicacion != null ? ubicacion.getLugarDesaparicion() : null,
                lat,
                lon,
                imagenUrl,
                thumbnail,
                report.getUser() != null ? report.getUser().getId() : null,
                report.getPet() != null ? report.getPet().getId() : null
        );
    }
}
