package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByReport_IdAndOwnerIdAndUserId2(UUID reportId, UUID ownerId, UUID userId2);
    Optional<Conversation> findByReport_Id(UUID reportId);
    List<Conversation> findByOwnerIdOrUserId2OrderByCreadoEnDesc(UUID ownerId, UUID userId2);
}
