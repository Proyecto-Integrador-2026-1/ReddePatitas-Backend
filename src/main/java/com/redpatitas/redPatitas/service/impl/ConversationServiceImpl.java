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

            // Asignar nombres
            ContactInfoResponse ownerInfo = userInfoMap.get(conv.getOwnerId());
            dto.setOwnerName(ownerInfo != null ? ownerInfo.nombre() + " " + ownerInfo.apellido() : "Usuario");
            ContactInfoResponse user2Info = userInfoMap.get(conv.getUserId2());
            dto.setUser2Name(user2Info != null ? user2Info.nombre() + " " + user2Info.apellido() : "Usuario");

            return dto;
        }).toList();
    }

    @Override
    public List<Conversation> listConversationsForUser(UUID userId) {
        return conversationRepository.findAll()
                .stream()
                .filter(c -> userId.equals(c.getOwnerId()) || userId.equals(c.getUserId2()))
                .collect(Collectors.toList());
    }

    @Override
    public Conversation getOrCreateConversationByReportId(UUID reportId, UUID userId) {
        // Try to find existing conversation for the report
        var existing = conversationRepository.findAll()
                .stream()
                .filter(c -> c.getReport() != null && reportId.equals(c.getReport().getId()))
                .findFirst();
        if (existing.isPresent()) return existing.get();

        // Create new conversation
        var report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        var conv = new Conversation();
        conv.setConversacionId(UUID.randomUUID());
        conv.setReport(report);
        conv.setOwnerId(userId);
        conv.setUserId2(userId);
        conv.setCreadoEn(Instant.now());

        return conversationRepository.save(conv);
    }
}
