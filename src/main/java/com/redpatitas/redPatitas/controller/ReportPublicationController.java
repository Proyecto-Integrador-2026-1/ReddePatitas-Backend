package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.request.ReportPublicationRequestDto;
import com.redpatitas.redPatitas.service.interfaces.ReportPublicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.context.SecurityContextHolder;

import com.redpatitas.redPatitas.security.JwtPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/report-publications")
@Tag(name = "Report Publication", description = "Endpoints para gestionar el reporte de publicaciones inapropiadas")
@RequiredArgsConstructor
public class ReportPublicationController {

    private final ReportPublicationService reportPublicationService;

    @Operation(summary = "Reportar una publicación inapropiada", description = "Permite a los usuarios reportar una publicación específica proporcionando un motivo y una descripción opcional. Si el usuario ya ha reportado la misma publicación, se devuelve un error de conflicto.")
    @PostMapping
    public ResponseEntity<Map<String, String>> submitReport(@Valid @RequestBody ReportPublicationRequestDto dto) {
        UUID reporterId = getReporterIdFromToken();

        String msg = reportPublicationService.submitReport(dto, reporterId);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    private UUID getReporterIdFromToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        try {
            var principal = (JwtPrincipal) auth.getPrincipal();
            return UUID.fromString(principal.userId());
        } catch (ClassCastException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
