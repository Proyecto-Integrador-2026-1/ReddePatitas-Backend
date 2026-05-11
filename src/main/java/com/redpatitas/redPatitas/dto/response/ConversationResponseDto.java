package com.redpatitas.redPatitas.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ConversationResponseDto {
    private UUID id;  // user_conversation.id
    private UUID conversationId;
    private UUID otherUserId;
    private String otherUserName;  // desde auth service
    private UUID reportId;
    private String lastMessage;
    private Instant lastMessageAt;
    private int unreadCount;
}
