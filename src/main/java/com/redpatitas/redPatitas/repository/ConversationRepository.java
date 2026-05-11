package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
	@Query("select c from Conversation c where c.report.id = :reportId")
	Optional<Conversation> findByReportId(@Param("reportId") UUID reportId);
}
