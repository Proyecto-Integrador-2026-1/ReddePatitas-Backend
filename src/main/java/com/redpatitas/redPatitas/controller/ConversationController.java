package com.redpatitas.redPatitas.controller;

import com.redpatitas.redPatitas.dto.request.SendMessageRequestDto;
import com.redpatitas.redPatitas.dto.response.ConversationResponseDto;
import com.redpatitas.redPatitas.dto.response.MessageResponseDto;
import com.redpatitas.redPatitas.service.interfaces.ConversationService;
import com.redpatitas.redPatitas.dto.response.ConversationsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Conversaciones", description = "Endpoints para gestionar conversaciones y mensajes")
@RequiredArgsConstructor
public class ConversationController {
    
    private final ConversationService conversationService;
    
    @PostMapping("/messages")
    @Operation(summary = "Enviar mensaje")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @Valid @RequestBody SendMessageRequestDto request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = parseUserId(userIdHeader);
        MessageResponseDto response = conversationService.sendMessage(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/conversations")
    @Operation(summary = "Listar conversaciones del usuario")
    public ResponseEntity<ConversationsResponse> listConversations(
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = parseUserId(userIdHeader);
        List<ConversationResponseDto> conversations = conversationService.getUserConversations(userId);
        
        // Calcular total de no leídos sumando el unreadCount de cada conversación
        long totalUnread = conversations.stream()
                .mapToLong(ConversationResponseDto::getUnreadCount)
                .sum();
        
        ConversationsResponse response = ConversationsResponse.builder()
                .conversations(conversations)
                .totalUnread(totalUnread)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/conversations/{userConversationId}/messages")
    @Operation(summary = "Obtener mensajes de una conversación")
    public ResponseEntity<List<MessageResponseDto>> getMessages(
            @PathVariable UUID userConversationId,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = parseUserId(userIdHeader);
        List<MessageResponseDto> messages = conversationService.getConversationMessages(userConversationId, userId);
        return ResponseEntity.ok(messages);
    }
    
    @PostMapping("/conversations/{userConversationId}/read")
    @Operation(summary = "Marcar mensajes como leídos")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID userConversationId,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = parseUserId(userIdHeader);
        conversationService.markAsRead(userConversationId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/conversations/{userConversationId}")
    @Operation(summary = "Eliminar conversación (soft delete)")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable UUID userConversationId,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = parseUserId(userIdHeader);
        conversationService.deleteConversation(userConversationId, userId);
        return ResponseEntity.noContent().build();
    }
    
    private UUID parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id requerido");
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-User-Id inválido");
        }
    }
}