package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.websocket.NotificationMessage;

import java.util.UUID;

/**
 * Servicio para manejar notificaciones en tiempo real vía WebSocket
 */
public interface WebSocketService {

    /**
     * Envía una notificación a un usuario específico
     *
     * @param userId  ID del usuario destino
     * @param message Mensaje de notificación
     */
    void sendToUser(UUID userId, NotificationMessage message);

    /**
     * Envía notificación de nuevo mensaje a un receptor
     *
     * @param receiverId         ID del usuario que recibe el mensaje
     * @param senderName         Nombre del remitente
     * @param content            Contenido del mensaje
     * @param conversationId     ID de la conversación compartida
     * @param userConversationId ID de la conversación del usuario (user_conversation)
     * @param senderId           ID del remitente
     * @param unreadCount        Cantidad de mensajes no leídos del receptor
     */
    void notifyNewMessage(UUID receiverId, String senderName, String content,
                          UUID conversationId, UUID userConversationId,
                          UUID senderId, int unreadCount);

    /**
     * Envía notificación de que los mensajes de una conversación han sido marcados como leídos
     *
     * @param userId         ID del usuario que marcó los mensajes como leídos
     * @param conversationId ID de la conversación
     * @param unreadCount    Cantidad restante de mensajes no leídos
     */
    void notifyMessagesRead(UUID userId, UUID conversationId, int unreadCount);

    /**
     * Envía notificación de que una conversación ha sido eliminada (soft delete)
     *
     * @param userId              ID del usuario que eliminó la conversación
     * @param userConversationId  ID de la conversación del usuario (user_conversation)
     */
    void notifyConversationDeleted(UUID userId, UUID userConversationId);
}
