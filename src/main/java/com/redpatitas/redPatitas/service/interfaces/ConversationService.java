package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.entity.Conversation;
import com.redpatitas.redPatitas.dto.response.ConversationResponseDto;
import java.util.List;
import java.util.UUID;

public interface ConversationService {
    Conversation getOrCreateConversationByReportId(UUID reportId, UUID requesterId);
    List<Conversation> listConversationsForUser(UUID userId);
    List<ConversationResponseDto> listConversationsDtoForUser(UUID userId);
}
