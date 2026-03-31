package com.redpatitas.redPatitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record ReportCreateDto(
        Long userId,
        Long petId,
        @NotBlank String tipoReporte,
        @NotBlank String estadoReporte,
        @NotNull Instant fechaEvento,
        Instant fechaCreacion,
        String lugarDesaparicion,
        BigDecimal latitud,
        BigDecimal longitud
) {
}
