package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.PetCreateDto;
import com.redpatitas.redPatitas.dto.PetResponseDto;
import com.redpatitas.redPatitas.service.interfaces.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Endpoints para gestionar mascotas")
public class PetController {

    private final PetService petService;

    @PostMapping
    @Operation(summary = "Crear nueva mascota", description = "Registra una nueva mascota")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mascota creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public CompletableFuture<ResponseEntity<PetResponseDto>> create(@Valid @RequestBody PetCreateDto dto) {
        return petService.create(dto).thenApply(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Listar todas las mascotas", description = "Retorna la lista de todas las mascotas registradas")
    @ApiResponse(responseCode = "200", description = "Lista de mascotas obtenida exitosamente")
    public CompletableFuture<ResponseEntity<List<PetResponseDto>>> findAll() {
        return petService.findAll().thenApply(ResponseEntity::ok);
    }
}
