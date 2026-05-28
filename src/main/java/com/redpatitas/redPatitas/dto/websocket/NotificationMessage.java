package com.redpatitas.redPatitas.dto.websocket;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class NotificationMessage {
    private String type;           // "NEW_MESSAGE", "MESSAGE_READ", "CONVERSATION_DELETED"
    private UUID conversationId;
    private UUID userConversationId;
    private UUID senderId;
    private String senderName;
    private String content;
    private String preview;
    private UUID receiverId;
    private long timestamp;
    private int unreadCount;
}