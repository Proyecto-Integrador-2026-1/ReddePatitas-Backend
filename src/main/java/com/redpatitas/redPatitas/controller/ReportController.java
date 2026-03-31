package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.ReportCreateDto;
import com.redpatitas.redPatitas.dto.ReportResponseDto;
import com.redpatitas.redPatitas.dto.request.ReportFrontendRequestDto;
import com.redpatitas.redPatitas.service.interfaces.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints para gestionar reportes de mascotas perdidas")
public class ReportController {

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear reporte desde JSON + imagen", 
               description = "Crea un reporte enviando el payload como JSON en parte multipart y la imagen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Payload inválido")
    })
    public CompletableFuture<ResponseEntity<ReportResponseDto>> create(
            @Valid @RequestPart("payload") String payload,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            ReportCreateDto dto = objectMapper.readValue(payload, ReportCreateDto.class);
            return reportService.create(dto, image).thenApply(ResponseEntity::ok);
        } catch (Exception e) {
            throw new IllegalArgumentException("Payload inválido para crear reporte", e);
        }
    }

    @PostMapping(value = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear reporte desde formulario frontend", 
               description = "Crea mascota y reporte en un solo endpoint, recibiendo formulario frontend + imagen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte y mascota creados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Payload inválido del formulario")
    })
    public CompletableFuture<ResponseEntity<ReportResponseDto>> createFromFrontendForm(
            @RequestPart("payload") String payload,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            ReportFrontendRequestDto dto = objectMapper.readValue(payload, ReportFrontendRequestDto.class);
            return reportService.createFromFrontendForm(dto, image).thenApply(ResponseEntity::ok);
        } catch (Exception e) {
            throw new IllegalArgumentException("Payload inválido del formulario frontend", e);
        }
    }

    @GetMapping
    @Operation(summary = "Listar todos los reportes", description = "Retorna la lista de todos los reportes creados")
    @ApiResponse(responseCode = "200", description = "Lista de reportes obtenida exitosamente")
    public CompletableFuture<ResponseEntity<List<ReportResponseDto>>> findAll() {
        return reportService.findAll().thenApply(ResponseEntity::ok);
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crear reporte desde JSON puro", 
               description = "Crea un reporte enviando el payload como JSON (sin imagen)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Payload inválido")
    })
    public CompletableFuture<ResponseEntity<ReportResponseDto>> createJson(
            @Valid @RequestBody ReportCreateDto dto
    ) {
        return reportService.create(dto, null).thenApply(ResponseEntity::ok);
    }
}
