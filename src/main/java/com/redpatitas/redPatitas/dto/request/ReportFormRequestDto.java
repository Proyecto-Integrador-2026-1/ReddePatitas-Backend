package com.redpatitas.redPatitas.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ReportFormRequestDto(
        Long userId,
        String estado,
        String tipo,
        @JsonProperty("tipo_otro") String tipoOtro,
        String nombre,
        String descripcion,
        @JsonProperty("fecha_desaparicion") LocalDate fechaDesaparicion,
        @JsonProperty("lugar_desaparicion") String lugarDesaparicion,
        BigDecimal latitud,
        BigDecimal longitud,
        @JsonProperty("creadoEn") Instant creadoEn
) {
}
