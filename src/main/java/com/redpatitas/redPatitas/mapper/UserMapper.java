package com.redpatitas.redPatitas.mapper;

import com.redpatitas.redPatitas.dto.request.UserCreateDto;
import com.redpatitas.redPatitas.dto.response.UserResponseDto;
import com.redpatitas.redPatitas.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserMapper {

    public User toEntity(UserCreateDto dto) {
        return User.builder()
                .nombre(dto.nombre())
                .apellido(dto.apellido())
                .telefono(dto.telefono())
                .contrasena(dto.contrasena())
                .fechaRegistro(Instant.now())
                .build();
    }

    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId().toString(),
                user.getNombre(),
                user.getApellido(),
                user.getTelefono(),
                user.getFechaRegistro()
        );
    }
}
