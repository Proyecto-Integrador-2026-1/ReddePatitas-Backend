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

import com.redpatitas.redPatitas.dto.response.ConversationResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ReportRepository reportRepository;
    private final com.redpatitas.redPatitas.repository.MessageRepository messageRepository;

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

    @Override
    public List<ConversationResponseDto> listConversationsDtoForUser(UUID userId) {
        var convs = listConversationsForUser(userId);
        return convs.stream().map(conv -> {
            var dto = new ConversationResponseDto();
            dto.setConversacionId(conv.getConversacionId());
            dto.setReportId(conv.getReport() != null ? conv.getReport().getId() : null);
            dto.setOwnerId(conv.getOwnerId());
            dto.setUserId2(conv.getUserId2());
            dto.setCreadoEn(conv.getCreadoEn());

            long unread = messageRepository.countUnreadByConversationForUser(conv.getConversacionId(), userId);
            dto.setUnreadCount(unread);

            return dto;
        }).toList();
    }
}
