package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.request.UserCreateDto;
import com.redpatitas.redPatitas.dto.response.UserResponseDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<UserResponseDto> create(UserCreateDto dto);

    CompletableFuture<List<UserResponseDto>> findAll();
}
