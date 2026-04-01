package com.redpatitas.redPatitas.mapper;

import com.redpatitas.redPatitas.dto.ReportCreateDto;
import com.redpatitas.redPatitas.dto.ReportResponseDto;
import com.redpatitas.redPatitas.dto.request.ReportFrontendRequestDto;
import com.redpatitas.redPatitas.entity.Pet;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ReportMapper {

    public Report toEntity(ReportCreateDto dto, User user, Pet pet) {
        return Report.builder()
                .user(user)
                .pet(pet)
                .tipoReporte(dto.tipoReporte())
                .estadoReporte(dto.estadoReporte())
                .fechaEvento(dto.fechaEvento())
                .fechaCreacion(dto.fechaCreacion() != null ? dto.fechaCreacion() : Instant.now())
                .lugarDesaparicion(dto.lugarDesaparicion())
                .build();
    }

    public ReportCreateDto fromFrontendDto(ReportFrontendRequestDto dto, Long petId) {
        return new ReportCreateDto(
                dto.userId(),
                petId,
                "perdida",
                dto.estado(),
                dto.fechaDesaparicion(),
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
                report.getEstadoReporte(),
                report.getFechaEvento(),
                report.getFechaCreacion(),
                report.getLugarDesaparicion(),
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
                report.getEstadoReporte(),
                report.getFechaEvento(),
                report.getFechaCreacion(),
                report.getLugarDesaparicion(),
                lat,
                lon,
                imagenUrl,
                thumbnail,
                report.getUser() != null ? report.getUser().getId() : null,
                report.getPet() != null ? report.getPet().getId() : null
        );
    }
}
