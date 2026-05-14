package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.request.ReportFormRequestDto;
import com.redpatitas.redPatitas.dto.response.ReportPrincipalResponseDto;
import com.redpatitas.redPatitas.dto.response.ReportResponseDto;
import com.redpatitas.redPatitas.service.interfaces.ReportService;
import com.redpatitas.redPatitas.dto.request.ResolveReportRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints para gestionar reportes de mascotas perdidas")
public class ReportController {

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "Listar reportes para página principal",
               description = "Retorna un arreglo directo de reportes con datos de mascota")
    @ApiResponse(responseCode = "200", description = "Lista de reportes obtenida exitosamente")
    public CompletableFuture<ResponseEntity<List<ReportPrincipalResponseDto>>> findAllForPrincipal() {
        return reportService.findAllForPrincipal().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/resolved")
    @Operation(summary = "Listar casos resueltos",
               description = "Retorna los reportes marcados como resueltos (Casos exitosos)")
    public CompletableFuture<ResponseEntity<List<ReportPrincipalResponseDto>>> findAllResolved() {
        return reportService.findAllResolvedForPrincipal().thenApply(ResponseEntity::ok);
    }

    @PostMapping(value = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear reporte desde formulario frontend", 
               description = "Crea mascota y reporte en un solo endpoint, recibiendo multipart con payload JSON + imagen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte y mascota creados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Payload inválido del formulario")
    })
    public CompletableFuture<ResponseEntity<ReportResponseDto>> createFromFrontendForm(
            @RequestPart("payload") String payload,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            ReportFormRequestDto dto = objectMapper.readValue(payload, ReportFormRequestDto.class);
            return reportService.createFromFrontendForm(dto, image).thenApply(ResponseEntity::ok);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload JSON inválido", exception);
        }
    }

    @PutMapping("/{id}/resolve")
    @Operation(summary = "Marcar reporte como resuelto",
               description = "Propietario marca su publicación como resuelta, con opción de indicar si se reencontró y dejar mensaje de agradecimiento")
        public CompletableFuture<ResponseEntity<ReportResponseDto>> resolveReport(
            @Parameter(description = "UUID del reporte", required = true)
            @PathVariable("id") UUID id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestBody ResolveReportRequestDto dto
    ) {
        return reportService.resolveReport(id, userIdHeader, dto).thenApply(ResponseEntity::ok);
    }

}
