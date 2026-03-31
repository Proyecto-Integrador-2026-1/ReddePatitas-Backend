package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.UserCreateDto;
import com.redpatitas.redPatitas.dto.UserResponseDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<UserResponseDto> create(UserCreateDto dto);

    CompletableFuture<List<UserResponseDto>> findAll();
}
