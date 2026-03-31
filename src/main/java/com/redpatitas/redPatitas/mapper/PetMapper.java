package com.redpatitas.redPatitas.mapper;

import com.redpatitas.redPatitas.dto.PetCreateDto;
import com.redpatitas.redPatitas.dto.PetResponseDto;
import com.redpatitas.redPatitas.entity.Pet;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

    public Pet toEntity(PetCreateDto dto) {
        return Pet.builder()
                .nombre(dto.nombre())
                .tipo(dto.tipo())
                .estado(dto.estado())
                .descripcion(dto.descripcion())
                .build();
    }

    public PetResponseDto toDto(Pet pet) {
        return new PetResponseDto(
                pet.getId(),
                pet.getNombre(),
                pet.getTipo(),
                pet.getEstado(),
                pet.getDescripcion()
        );
    }
}
