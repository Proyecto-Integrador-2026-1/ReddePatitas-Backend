package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    Optional<Message> findTopByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'LEIDO' WHERE m.conversationId = :conversationId AND m.senderId <> :userId AND m.status = 'ENVIADO'")
    int markConversationMessagesAsRead(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

    @Query("select count(m) from Message m where m.conversationId = :conversationId and m.status = 'ENVIADO' and m.senderId <> :userId")
    long countUnreadByConversationForUser(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}