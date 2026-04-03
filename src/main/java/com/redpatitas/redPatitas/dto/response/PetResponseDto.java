package com.redpatitas.redPatitas.dto.response;

public record PetResponseDto(
        Long id,
        String nombre,
        String tipo,
        String estado,
        String descripcion
) {
}