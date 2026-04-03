package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.request.ReportCreateDto;
import com.redpatitas.redPatitas.dto.request.ReportFormRequestDto;
import com.redpatitas.redPatitas.dto.response.ReportPrincipalResponseDto;
import com.redpatitas.redPatitas.dto.response.ReportResponseDto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReportService {
    CompletableFuture<ReportResponseDto> create(ReportCreateDto dto, MultipartFile image);

    CompletableFuture<ReportResponseDto> createFromFrontendForm(ReportFormRequestDto dto, MultipartFile image);

    CompletableFuture<List<ReportResponseDto>> findAll();

    CompletableFuture<List<ReportPrincipalResponseDto>> findAllForPrincipal();
}
