package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.request.PetCreateDto;
import com.redpatitas.redPatitas.dto.response.PetResponseDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PetService {
    CompletableFuture<PetResponseDto> create(PetCreateDto dto);

    CompletableFuture<List<PetResponseDto>> findAll();
}
