package com.redpatitas.redPatitas.mapper;

import com.redpatitas.redPatitas.dto.response.ReportResponseDto;
import com.redpatitas.redPatitas.entity.Imagen;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.entity.Ubicacion;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public ReportResponseDto toDto(Report report, Imagen imagen, Ubicacion ubicacion) {
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
                report.getUserId(),
                report.getPet() != null ? report.getPet().getId() : null
        );
    }
}