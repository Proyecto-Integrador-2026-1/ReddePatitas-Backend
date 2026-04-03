package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.dto.request.PetCreateDto;
import com.redpatitas.redPatitas.dto.response.PetResponseDto;
import com.redpatitas.redPatitas.mapper.PetMapper;
import com.redpatitas.redPatitas.repository.PetRepository;
import com.redpatitas.redPatitas.repository.UserRepository;
import com.redpatitas.redPatitas.service.interfaces.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetMapper petMapper;

    @Override
    @Async
    public CompletableFuture<PetResponseDto> create(PetCreateDto dto) {
        var user = dto.userId() != null ? userRepository.findById(dto.userId()).orElse(null) : null;
        var saved = petRepository.save(petMapper.toEntity(dto, user));
        return CompletableFuture.completedFuture(petMapper.toDto(saved));
    }

    @Override
    @Async
    public CompletableFuture<List<PetResponseDto>> findAll() {
        var pets = petRepository.findAll().stream().map(petMapper::toDto).toList();
        return CompletableFuture.completedFuture(pets);
    }
}
