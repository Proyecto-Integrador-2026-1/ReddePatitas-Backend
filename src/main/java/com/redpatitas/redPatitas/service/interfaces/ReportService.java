package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.ReportCreateDto;
import com.redpatitas.redPatitas.dto.ReportResponseDto;
import com.redpatitas.redPatitas.dto.request.ReportFrontendRequestDto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReportService {
    CompletableFuture<ReportResponseDto> create(ReportCreateDto dto, MultipartFile image);

    CompletableFuture<ReportResponseDto> createFromFrontendForm(ReportFrontendRequestDto dto, MultipartFile image);

    CompletableFuture<List<ReportResponseDto>> findAll();
}
