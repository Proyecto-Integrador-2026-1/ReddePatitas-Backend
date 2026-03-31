package com.redpatitas.redPatitas.dto;

import jakarta.validation.constraints.NotBlank;

public record PetCreateDto(
        @NotBlank String nombre,
        @NotBlank String tipo,
        @NotBlank String estado,
        @NotBlank String descripcion
) {
}
