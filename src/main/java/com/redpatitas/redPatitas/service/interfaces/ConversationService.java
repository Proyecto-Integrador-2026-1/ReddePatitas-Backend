package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.request.SendMessageRequestDto;
import com.redpatitas.redPatitas.dto.response.ConversationResponseDto;
import com.redpatitas.redPatitas.dto.response.MessageResponseDto;

import java.util.List;
import java.util.UUID;

public interface ConversationService {
    MessageResponseDto sendMessage(SendMessageRequestDto request, UUID senderId);
    List<ConversationResponseDto> getUserConversations(UUID userId);
    List<MessageResponseDto> getConversationMessages(UUID userConversationId, UUID userId);
    int markAsRead(UUID userConversationId, UUID userId);
    void deleteConversation(UUID userConversationId, UUID userId);
    long countUnreadForUser(UUID userId);
    long countUnreadByConversation(UUID userConversationId, UUID userId);
}
