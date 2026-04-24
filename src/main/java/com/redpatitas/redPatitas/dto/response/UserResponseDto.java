package com.redpatitas.redPatitas.dto.response;

import java.time.Instant;

public record UserResponseDto(
        String id,
        String nombre,
        String apellido,
        String telefono,
        Instant fechaRegistro
) {
}