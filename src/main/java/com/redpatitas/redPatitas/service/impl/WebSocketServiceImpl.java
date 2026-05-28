package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.dto.websocket.NotificationMessage;
import com.redpatitas.redPatitas.service.interfaces.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToUser(UUID userId, NotificationMessage message) {
        log.info("Enviando notificación a usuario {}: {}", userId, message.getType());
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            message
        );
    }

    @Override
    public void notifyNewMessage(UUID receiverId, String senderName, String content,
                                  UUID conversationId, UUID userConversationId,
                                  UUID senderId, int unreadCount) {
        
        String preview = content.length() > 100 ? content.substring(0, 100) + "..." : content;
        
        NotificationMessage message = NotificationMessage.builder()
                .type("NEW_MESSAGE")
                .conversationId(conversationId)
                .userConversationId(userConversationId)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .preview(preview)
                .receiverId(receiverId)
                .unreadCount(unreadCount)
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendToUser(receiverId, message);
    }

    @Override
    public void notifyMessagesRead(UUID userId, UUID conversationId, int unreadCount) {
        NotificationMessage message = NotificationMessage.builder()
                .type("MESSAGE_READ")
                .conversationId(conversationId)
                .unreadCount(unreadCount)
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendToUser(userId, message);
    }

    @Override
    public void notifyConversationDeleted(UUID userId, UUID userConversationId) {
        NotificationMessage message = NotificationMessage.builder()
                .type("CONVERSATION_DELETED")
                .userConversationId(userConversationId)
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendToUser(userId, message);
    }
}