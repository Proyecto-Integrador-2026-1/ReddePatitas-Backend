package com.redpatitas.redPatitas.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReportResponseDto(
        Long id,
        String tipoReporte,
        String estadoReporte,
        Instant fechaEvento,
        Instant fechaCreacion,
        String lugarDesaparicion,
        BigDecimal latitud,
        BigDecimal longitud,
        String imagenUrl,
        String thumbnailUrl,
        Long userId,
        Long petId
) {
}
