package com.redpatitas.redPatitas.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ReportResponseDto(
        Long id,
        String tipoReporte,
        Instant fechaEvento,
        Instant fechaCreacion,
        String lugarDesaparicion,
        BigDecimal latitud,
        BigDecimal longitud,
        String imagenUrl,
        String thumbnailUrl,
        String userId,
        Long petId
) {
}