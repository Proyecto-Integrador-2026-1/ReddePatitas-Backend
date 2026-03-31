package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.ReportResponseDto;
import com.redpatitas.redPatitas.dto.request.ReportFrontendRequestDto;
import com.redpatitas.redPatitas.service.interfaces.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints para gestionar reportes de mascotas perdidas")
@Validated
public class ReportController {

    private final ReportService reportService;

    @PostMapping(value = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear reporte desde formulario frontend", 
               description = "Crea mascota y reporte en un solo endpoint, recibiendo FormData campo por campo + imagen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte y mascota creados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Payload inválido del formulario")
    })
    public CompletableFuture<ResponseEntity<ReportResponseDto>> createFromFrontendForm(
            @NotBlank @RequestParam("estado") String estado,
            @NotBlank @RequestParam("tipo") String tipo,
            @RequestParam(value = "tipo_otro", required = false) String tipoOtro,
            @RequestParam(value = "nombre", required = false) String nombre,
            @NotBlank @RequestParam("descripcion") String descripcion,
            @NotBlank @RequestParam("fecha_desaparicion") String fechaDesaparicion,
            @NotBlank @RequestParam("lugar_desaparicion") String lugarDesaparicion,
            @NotBlank @RequestParam("latitud") String latitud,
            @NotBlank @RequestParam("longitud") String longitud,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        ReportFrontendRequestDto dto = new ReportFrontendRequestDto(
                null,
                estado,
                tipo,
                tipoOtro,
                nombre,
                descripcion,
                parseFrontendDate(fechaDesaparicion),
                lugarDesaparicion,
                parseDecimal("latitud", latitud),
                parseDecimal("longitud", longitud),
                null
        );
        return reportService.createFromFrontendForm(dto, image).thenApply(ResponseEntity::ok);
    }

    private Instant parseFrontendDate(String rawDate) {
        try {
            return Instant.parse(rawDate);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(rawDate).atStartOfDay().toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("fecha_desaparicion debe tener formato ISO-8601 o yyyy-MM-dd", e);
            }
        }
    }

    private BigDecimal parseDecimal(String fieldName, String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " debe ser numérico", e);
        }
    }

}
