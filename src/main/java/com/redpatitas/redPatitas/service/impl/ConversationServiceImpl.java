package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.entity.Conversation;
import com.redpatitas.redPatitas.repository.ConversationRepository;
import com.redpatitas.redPatitas.repository.MessageRepository;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.service.interfaces.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.redpatitas.redPatitas.dto.response.ConversationResponseDto;
import com.redpatitas.redPatitas.dto.response.ContactInfoResponse;
import com.redpatitas.redPatitas.client.AuthServiceClient;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ReportRepository reportRepository;
    private final AuthServiceClient authServiceClient;   // nuevo

    @Override
    public List<ConversationResponseDto> listConversationsDtoForUser(UUID userId) {
        var convs = listConversationsForUser(userId);
        if (convs.isEmpty()) return List.of();

        // Recopilar todos los IDs de participantes (owner y userId2)
        List<UUID> participantIds = convs.stream()
            .flatMap(conv -> Stream.of(conv.getOwnerId(), conv.getUserId2()))
            .distinct()
            .collect(Collectors.toList());

        // Obtener información de contacto en batch desde auth
        Map<UUID, ContactInfoResponse> userInfoMap = authServiceClient.getBatchContactInfo(participantIds);

        return convs.stream().map(conv -> {
            var dto = new ConversationResponseDto();
            dto.setConversacionId(conv.getConversacionId());
            dto.setReportId(conv.getReport().getId());
            dto.setOwnerId(conv.getOwnerId());
            dto.setUserId2(conv.getUserId2());
            dto.setCreadoEn(conv.getCreadoEn());
            dto.setUnreadCount(messageRepository.countUnreadByConversationForUser(conv.getConversacionId(), userId));

        
            ContactInfoResponse ownerInfo = userInfoMap.get(conv.getOwnerId());
            dto.setOwnerName(ownerInfo != null ? ownerInfo.nombre() + " " + ownerInfo.apellido() : "Usuario");
            ContactInfoResponse user2Info = userInfoMap.get(conv.getUserId2());
            dto.setUser2Name(user2Info != null ? user2Info.nombre() + " " + user2Info.apellido() : "Usuario");

            String publisherName = userId.equals(conv.getOwnerId()) ? dto.getUser2Name() : dto.getOwnerName();
            dto.setPublisherName(publisherName);

            return dto;
        }).toList();
    }

    @Override
    public List<Conversation> listConversationsForUser(UUID userId) {
        return conversationRepository.findByOwnerIdOrUserId2OrderByCreadoEnDesc(userId, userId);
    }

    @Override
    public Conversation getOrCreateConversationByReportId(UUID reportId, UUID userId) {
        var existing = conversationRepository.findByReport_Id(reportId);
        if (existing.isPresent()) {
            var conv = existing.get();
            if (!userId.equals(conv.getOwnerId()) &&
                    (conv.getUserId2() == null || conv.getUserId2().equals(conv.getOwnerId()))) {
                conv.setUserId2(userId);
                return conversationRepository.save(conv);
            }
            return conv;
        }

        
        var report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        var conv = new Conversation();
        conv.setReport(report);
        UUID ownerId = report.getUserId();
        conv.setOwnerId(ownerId);
        conv.setUserId2(userId.equals(ownerId) ? ownerId : userId);
        conv.setCreadoEn(Instant.now());

        try {
            return conversationRepository.save(conv);
        } catch (DataIntegrityViolationException ex) {
            return conversationRepository.findByReport_Id(reportId)
                    .orElseThrow(() -> ex);
        }
    }
}
