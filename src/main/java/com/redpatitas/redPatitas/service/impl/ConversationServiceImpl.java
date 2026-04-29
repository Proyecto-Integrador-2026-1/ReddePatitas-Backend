package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.entity.Conversation;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.repository.ConversationRepository;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.service.interfaces.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ReportRepository reportRepository;

    @Override
    public Conversation getOrCreateConversationByReportId(UUID reportId, UUID requesterId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
        UUID ownerId = report.getUserId();
        // try find existing conversation where owner is report owner and user2 is requester
        var maybe = conversationRepository.findByReport_IdAndOwnerIdAndUserId2(reportId, ownerId, requesterId);
        if (maybe.isPresent()) return maybe.get();

        // create new conversation
        Conversation conv = Conversation.builder()
                .report(report)
                .ownerId(ownerId)
                .userId2(requesterId)
                .creadoEn(Instant.now())
                .build();
        return conversationRepository.save(conv);
    }

    @Override
    public List<Conversation> listConversationsForUser(UUID userId) {
        return conversationRepository.findByOwnerIdOrUserId2OrderByCreadoEnDesc(userId, userId);
    }
}
