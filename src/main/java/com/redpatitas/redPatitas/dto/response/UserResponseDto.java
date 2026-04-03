package com.redpatitas.redPatitas.dto.response;

import java.time.Instant;

public record UserResponseDto(
        Long id,
        String nombre,
        String apellido,
        String telefono,
        Instant fechaRegistro
) {
}