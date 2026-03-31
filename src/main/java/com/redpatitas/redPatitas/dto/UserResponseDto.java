package com.redpatitas.redPatitas.dto;

import java.time.Instant;

public record UserResponseDto(
        Long id,
        String nombre,
        String apellido,
        String telefono,
        Instant fechaRegistro
) {
}
