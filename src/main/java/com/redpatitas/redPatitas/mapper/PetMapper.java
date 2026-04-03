package com.redpatitas.redPatitas.mapper;

import com.redpatitas.redPatitas.dto.request.PetCreateDto;
import com.redpatitas.redPatitas.dto.response.PetResponseDto;
import com.redpatitas.redPatitas.entity.Pet;
import com.redpatitas.redPatitas.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

    public Pet toEntity(PetCreateDto dto) {
        return toEntity(dto, null);
    }

    public Pet toEntity(PetCreateDto dto, User user) {
        return Pet.builder()
                .user(user)
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
