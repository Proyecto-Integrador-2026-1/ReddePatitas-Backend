package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.dto.response.MessageResponseDto;
import com.redpatitas.redPatitas.entity.Conversation;
import com.redpatitas.redPatitas.entity.Message;
import com.redpatitas.redPatitas.repository.ConversationRepository;
import com.redpatitas.redPatitas.repository.MessageRepository;
import com.redpatitas.redPatitas.service.interfaces.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final com.redpatitas.redPatitas.service.interfaces.ConversationService conversationService; // to get/create conv
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MessageResponseDto sendMessage(UUID reportId, UUID conversacionId, UUID remitenteId, String contenido) {
        Conversation conv;
        if (conversacionId != null) {
            conv = conversationRepository.findById(conversacionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
            // verificar que el remitente es participante
            UUID owner = conv.getOwnerId();
            UUID user2 = conv.getUserId2();
            if (!(remitenteId.equals(owner) || (user2 != null && remitenteId.equals(user2)))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No está autorizado para enviar mensajes en esta conversación");
            }
        } else {
            conv = conversationService.getOrCreateConversationByReportId(reportId, remitenteId);
        }

        Message msg = Message.builder()
                .conversation(conv)
                .remitenteId(remitenteId)
                .contenido(contenido)
                .estado("NO_LEIDO")
                .creadoEn(Instant.now())
                .build();

        Message saved = messageRepository.save(msg);

        // publish event for real-time listeners
        try {
            eventPublisher.publishEvent(saved);
        } catch (Exception ignored) {
        }

        return mapToDto(saved);
    }

    @Override
    public List<MessageResponseDto> getConversationMessages(UUID conversacionId, UUID requesterId) {
        Conversation conv = conversationRepository.findById(conversacionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
        UUID owner = conv.getOwnerId();
        UUID user2 = conv.getUserId2();
        if (!(requesterId.equals(owner) || (user2 != null && requesterId.equals(user2)))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No está autorizado para ver mensajes de esta conversación");
        }
        List<Message> msgs = messageRepository.findByConversation_ConversacionIdOrderByCreadoEnAsc(conversacionId);
        return msgs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int markConversationAsRead(UUID conversacionId, UUID requesterId) {
        Conversation conv = conversationRepository.findById(conversacionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
        UUID owner = conv.getOwnerId();
        UUID user2 = conv.getUserId2();
        if (!(requesterId.equals(owner) || (user2 != null && requesterId.equals(user2)))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No está autorizado para marcar mensajes en esta conversación");
        }
        // Bulk update: marcar como LEIDO todos los mensajes NO_LEIDO cuyo remitente != requesterId
        int updated = messageRepository.markConversationMessagesAsRead(conversacionId, requesterId);
        return updated;
    }

    private MessageResponseDto mapToDto(Message m) {
        return new MessageResponseDto(
                m.getMensajeId(),
                m.getConversation() != null ? m.getConversation().getConversacionId() : null,
                m.getRemitenteId(),
                m.getContenido(),
                m.getEstado(),
                m.getCreadoEn()
        );
    }
}
