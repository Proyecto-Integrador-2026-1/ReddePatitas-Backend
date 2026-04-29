package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversation_ConversacionIdOrderByCreadoEnAsc(UUID conversacionId);
    List<Message> findByConversation_ConversacionIdAndEstadoOrderByCreadoEnAsc(UUID conversacionId, String estado);
    long countByConversation_ConversacionIdAndEstado(UUID conversacionId, String estado);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.estado = 'LEIDO' WHERE m.conversation.conversacionId = :conversationId AND m.estado = 'NO_LEIDO' AND m.remitenteId <> :requesterId")
    int markConversationMessagesAsRead(@Param("conversationId") UUID conversationId, @Param("requesterId") UUID requesterId);
}
