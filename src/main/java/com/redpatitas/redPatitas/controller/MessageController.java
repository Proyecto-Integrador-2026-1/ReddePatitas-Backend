package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.request.SendMessageRequestDto;
import com.redpatitas.redPatitas.dto.response.MessageResponseDto;
import com.redpatitas.redPatitas.service.interfaces.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.redpatitas.redPatitas.service.interfaces.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Messages", description = "Endpoints para gestionar mensajes y conversaciones entre usuarios interesados en reportes")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    @PostMapping("/messages")
    @Operation(summary = "Enviar mensaje en una conversación o iniciar una nueva por reporte", description = "Si se proporciona conversacionId, se envía el mensaje ahí (si el remitente es parte). Si no, se inicia o reutiliza una conversación relacionada al reportId.")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @Valid @RequestBody SendMessageRequestDto dto,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID remitenteId = parseUserIdHeader(userIdHeader);

        if ((dto.getConversacionId() == null) && (dto.getReportId() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reportId o conversacionId requerido");
        }

        MessageResponseDto resp = messageService.sendMessage(dto.getReportId(), dto.getConversacionId(), remitenteId, dto.getContenido());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Listar conversaciones para un usuario", description = "Obtiene la lista de conversaciones relacionadas a un usuario específico")
    @GetMapping("/conversations")
    public ResponseEntity<List<?>> listConversations(@RequestHeader(name = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = parseUserIdHeader(userIdHeader);
        var convs = conversationService.listConversationsForUser(userId);
        return ResponseEntity.ok(convs);
    }

    @Operation(summary = "Obtener mensajes de una conversación", description = "Obtiene la lista de mensajes relacionados a una conversación específica")
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponseDto>> getConversationMessages(
            @PathVariable UUID conversationId,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID userId = parseUserIdHeader(userIdHeader);

        List<MessageResponseDto> msgs = messageService.getConversationMessages(conversationId, userId);
        return ResponseEntity.ok(msgs);
    }

    @Operation(summary = "Marcar mensajes de una conversación como leídos", description = "Marca los mensajes de una conversación específica como leídos")
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable UUID conversationId,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID userId = parseUserIdHeader(userIdHeader);

        int updated = messageService.markConversationAsRead(conversationId, userId);
        return ResponseEntity.ok(Map.of("message", "Mensajes marcados como leídos", "updated", updated));
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
