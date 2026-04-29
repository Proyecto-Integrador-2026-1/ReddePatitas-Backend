package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.response.MessageResponseDto;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponseDto sendMessage(UUID reportId, UUID conversacionId, UUID remitenteId, String contenido);
    List<MessageResponseDto> getConversationMessages(UUID conversacionId, UUID requesterId);
    int markConversationAsRead(UUID conversacionId, UUID requesterId);
}
