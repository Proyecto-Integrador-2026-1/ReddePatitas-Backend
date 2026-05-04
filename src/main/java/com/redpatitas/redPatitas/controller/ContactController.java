package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.response.ContactResponseDto;
import com.redpatitas.redPatitas.service.interfaces.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Contact", description = "Endpoints para obtener información de contacto de reportes y mascotas")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(summary = "Obtener el id del publicador de un reporte (ownerId)", description = "Retorna el id del publicador de un reporte específico. Si el requester es el publicador, se indica que es su publicación.")
    @GetMapping("/reports/{reportId}/contact")
    public ResponseEntity<ContactResponseDto> getReportContact(
            @PathVariable UUID reportId,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID requesterId = parseUserIdHeader(userIdHeader);
        ContactResponseDto resp = contactService.getContactByReportId(reportId, requesterId);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Obtener el id del publicador de una mascota (ownerId)", description = "Retorna el id del publicador de una mascota específica. Si el requester es el publicador, se indica que es su publicación.")
    @GetMapping("/pets/{petId}/contact")
    public ResponseEntity<ContactResponseDto> getPetContact(
            @PathVariable UUID petId,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID requesterId = parseUserIdHeader(userIdHeader);
        ContactResponseDto resp = contactService.getContactByPetId(petId, requesterId);
        return ResponseEntity.ok(resp);
    }

    private UUID parseUserIdHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cabecera X-User-Id inválida");
        }
    }
}
