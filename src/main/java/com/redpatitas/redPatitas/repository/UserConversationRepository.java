package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.UserConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserConversationRepository extends JpaRepository<UserConversation, UUID> {
    
    @Query("SELECT uc FROM UserConversation uc WHERE uc.userId = :userId AND uc.deletedAt IS NULL ORDER BY uc.lastMessageAt DESC")
    List<UserConversation> findActiveByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT uc FROM UserConversation uc WHERE uc.userId = :userId AND uc.otherUserId = :otherUserId AND uc.reportId = :reportId AND uc.deletedAt IS NULL")
    Optional<UserConversation> findActiveByUserAndOtherAndReport(
        @Param("userId") UUID userId,
        @Param("otherUserId") UUID otherUserId,
        @Param("reportId") UUID reportId);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserConversation uc SET uc.deletedAt = CURRENT_TIMESTAMP WHERE uc.id = :id AND uc.userId = :userId")
    int softDeleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
    
    List<UserConversation> findByConversationId(UUID conversationId);
}