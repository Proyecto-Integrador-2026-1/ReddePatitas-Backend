package com.redpatitas.redPatitas.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateDto(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank String telefono,
        @NotBlank String contrasena
) {
}
