// package com.redpatitas.redPatitas.controller;

// import com.redpatitas.redPatitas.dto.UserCreateDto;
// import com.redpatitas.redPatitas.dto.UserResponseDto;
// import com.redpatitas.redPatitas.service.interfaces.UserService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.List;
// import java.util.concurrent.CompletableFuture;

// @RestController
// @RequestMapping("/api/users")
// @RequiredArgsConstructor
// @Tag(name = "Users", description = "Endpoints para gestionar usuarios")
// public class UserController {

//     private final UserService userService;

//     @PostMapping
//     @Operation(summary = "Crear nuevo usuario", description = "Registra un nuevo usuario en el sistema")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente"),
//             @ApiResponse(responseCode = "400", description = "Datos inválidos")
//     })
//     public CompletableFuture<ResponseEntity<UserResponseDto>> create(@Valid @RequestBody UserCreateDto dto) {
//         return userService.create(dto).thenApply(ResponseEntity::ok);
//     }

//     @GetMapping
//     @Operation(summary = "Listar todos los usuarios", description = "Retorna la lista de todos los usuarios registrados")
//     @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
//     public CompletableFuture<ResponseEntity<List<UserResponseDto>>> findAll() {
//         return userService.findAll().thenApply(ResponseEntity::ok);
//     }
// }
