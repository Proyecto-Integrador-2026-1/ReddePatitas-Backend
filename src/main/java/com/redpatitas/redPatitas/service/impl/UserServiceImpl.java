package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.dto.UserCreateDto;
import com.redpatitas.redPatitas.dto.UserResponseDto;
import com.redpatitas.redPatitas.mapper.UserMapper;
import com.redpatitas.redPatitas.repository.UserRepository;
import com.redpatitas.redPatitas.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Async
    public CompletableFuture<UserResponseDto> create(UserCreateDto dto) {
        var saved = userRepository.save(userMapper.toEntity(dto));
        return CompletableFuture.completedFuture(userMapper.toDto(saved));
    }

    @Override
    @Async
    public CompletableFuture<List<UserResponseDto>> findAll() {
        var users = userRepository.findAll().stream().map(userMapper::toDto).toList();
        return CompletableFuture.completedFuture(users);
    }
}
