package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.request.ReportFormRequestDto;
import com.redpatitas.redPatitas.dto.response.ReportPrincipalResponseDto;
import com.redpatitas.redPatitas.dto.response.ReportResponseDto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReportService {
    CompletableFuture<ReportResponseDto> createFromFrontendForm(ReportFormRequestDto dto, MultipartFile image);
    CompletableFuture<List<ReportResponseDto>> findAll();
    CompletableFuture<List<ReportPrincipalResponseDto>> findAllForPrincipal();
    CompletableFuture<List<ReportPrincipalResponseDto>> findAllResolvedForPrincipal();
    CompletableFuture<ReportResponseDto> resolveReport(java.util.UUID reportId, String userIdHeader, com.redpatitas.redPatitas.dto.request.ResolveReportRequestDto dto);
    int deleteReportsOlderThan14Days();
}
