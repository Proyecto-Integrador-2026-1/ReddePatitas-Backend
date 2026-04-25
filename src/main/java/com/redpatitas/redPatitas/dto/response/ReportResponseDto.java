package com.redpatitas.redPatitas.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReportResponseDto(
        UUID id,
        String tipoReporte,
        Instant fechaEvento,
        Instant fechaCreacion,
        String lugarDesaparicion,
        BigDecimal latitud,
        BigDecimal longitud,
        String imagenUrl,
        String thumbnailUrl,
        UUID userId,
        UUID petId
) {
}