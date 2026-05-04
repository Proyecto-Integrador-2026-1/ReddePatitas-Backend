package com.redpatitas.redPatitas.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReportPrincipalResponseDto(
        UUID id,                          // ← antes Long
        UUID userId,                      // nuevo campo para el ID del usuario
        String nombre,
        String estado,
        String tipo,
        String descripcion,
        String lugar_desaparicion,
        BigDecimal latitud,
        BigDecimal longitud,
        String imagen_url,
        String thumbnail_url,
        Instant fecha_publicacion,
        Instant fecha_desaparicion
) {
}