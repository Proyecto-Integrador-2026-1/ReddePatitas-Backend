package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.client.AuthServiceClient;
import com.redpatitas.redPatitas.dto.request.SendMessageRequestDto;
import com.redpatitas.redPatitas.dto.response.ContactInfoResponse;
import com.redpatitas.redPatitas.dto.response.ConversationResponseDto;
import com.redpatitas.redPatitas.dto.response.MessageResponseDto;
import com.redpatitas.redPatitas.entity.Conversation;
import com.redpatitas.redPatitas.entity.Message;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.entity.UserConversation;
import com.redpatitas.redPatitas.repository.ConversationRepository;
import com.redpatitas.redPatitas.repository.MessageRepository;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.repository.UserConversationRepository;
import com.redpatitas.redPatitas.service.interfaces.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserConversationRepository userConversationRepository;
    private final ReportRepository reportRepository;
    private final AuthServiceClient authServiceClient;
    
    @Override
    @Transactional
    public MessageResponseDto sendMessage(SendMessageRequestDto request, UUID senderId) {
        Conversation conversation = resolveConversation(request);
        Report report = conversation.getReport();
        UUID reportId = report != null ? report.getId() : request.getReportId();
        UUID receiverId = request.getReceiverId();
        if (receiverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo determinar el receptor");
        }

        // 1. Crear y guardar mensaje
        Message message = Message.builder()
            .conversationId(conversation.getConversacionId())
            .senderId(senderId)
            .content(request.getContent())
            .status("ENVIADO")
            .createdAt(Instant.now())
            .build();
        message = messageRepository.save(message);

        // 2. Actualizar o crear user_conversation para ambos usuarios
        updateUserConversations(conversation.getConversacionId(), reportId, senderId, receiverId);

        // 3. Actualizar último mensaje y contadores
        updateConversationMetadata(conversation.getConversacionId());

        // 4. Obtener nombre del remitente
        ContactInfoResponse senderInfo = authServiceClient.getContactInfo(senderId);
        String senderName = senderInfo != null ? senderInfo.nombre() + " " + senderInfo.apellido() : "Usuario";

        return MessageResponseDto.builder()
            .id(message.getId())
            .senderId(message.getSenderId())
                .senderName(senderName)
            .content(message.getContent())
            .status(message.getStatus())
            .createdAt(message.getCreatedAt())
                .build();
    }

    private Conversation resolveConversation(SendMessageRequestDto request) {
        UUID reportId = request.getReportId();
        if (reportId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reportId requerido");
        }

        return conversationRepository.findByReportId(reportId)
                .orElseGet(() -> {
                    Report report = reportRepository.findById(reportId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
                    Conversation conversation = Conversation.builder()
                            .report(report)
                            .creadoEn(Instant.now())
                            .build();
                    return conversationRepository.save(conversation);
                });
    }

    private void updateUserConversations(UUID conversationId, UUID reportId, UUID senderId, UUID receiverId) {
        // Para el remitente
        if (userConversationRepository.findActiveByUserAndOtherAndReport(senderId, receiverId, reportId).isEmpty()) {
            createUserConversation(senderId, receiverId, conversationId, reportId);
        }
        
        // Para el receptor
        if (userConversationRepository.findActiveByUserAndOtherAndReport(receiverId, senderId, reportId).isEmpty()) {
            createUserConversation(receiverId, senderId, conversationId, reportId);
        }
    }
    
    private void createUserConversation(UUID userId, UUID otherUserId, UUID conversationId, UUID reportId) {
        UserConversation uc = UserConversation.builder()
                .userId(userId)
                .otherUserId(otherUserId)
                .conversationId(conversationId)
                .reportId(reportId)
                .unreadCount(0)
                .createdAt(Instant.now())
                .build();
        userConversationRepository.save(uc);
    }
    
    private void updateConversationMetadata(UUID conversationId) {
        var lastMessage = messageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversationId);
        
        if (lastMessage.isPresent()) {
            Message msg = lastMessage.get();
            List<UserConversation> userConvs = userConversationRepository.findByConversationId(conversationId);
            
            for (UserConversation uc : userConvs) {
                uc.setLastMessage(msg.getContent());
                uc.setLastMessageAt(msg.getCreatedAt());

                long unread = messageRepository.countUnreadByConversationForUser(conversationId, uc.getUserId());
                uc.setUnreadCount((int) unread);
                uc.setUpdatedAt(Instant.now());
                
                userConversationRepository.save(uc);
            }
        }
    }
    
    @Override
    public List<ConversationResponseDto> getUserConversations(UUID userId) {
        List<UserConversation> userConvs = userConversationRepository.findActiveByUserId(userId);
        
        // Obtener nombres de los otros usuarios en batch
        List<UUID> otherUserIds = userConvs.stream()
                .map(UserConversation::getOtherUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<UUID, ContactInfoResponse> contactInfoMap = authServiceClient.getBatchContactInfo(otherUserIds);
        
        return userConvs.stream().map(uc -> {
            ContactInfoResponse otherUser = contactInfoMap.get(uc.getOtherUserId());
            String otherName = otherUser != null ? otherUser.nombre() + " " + otherUser.apellido() : "Usuario";
            
            return ConversationResponseDto.builder()
                    .id(uc.getId())
                    .conversationId(uc.getConversationId())
                    .otherUserId(uc.getOtherUserId())
                    .otherUserName(otherName)
                    .reportId(uc.getReportId())
                    .lastMessage(uc.getLastMessage())
                    .lastMessageAt(uc.getLastMessageAt())
                    .unreadCount(uc.getUnreadCount())
                    .build();
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<MessageResponseDto> getConversationMessages(UUID userConversationId, UUID userId) {
        UserConversation uc = userConversationRepository.findById(userConversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
        
        if (!uc.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta conversación");
        }
        
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(uc.getConversationId());
        
        // Obtener nombres de los remitentes
        List<UUID> senderIds = messages.stream()
            .map(Message::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<UUID, ContactInfoResponse> contactInfoMap = authServiceClient.getBatchContactInfo(senderIds);
        
        return messages.stream().map(msg -> {
            ContactInfoResponse sender = contactInfoMap.get(msg.getSenderId());
            String senderName = sender != null ? sender.nombre() + " " + sender.apellido() : "Usuario";
            
            return MessageResponseDto.builder()
                    .id(msg.getId())
                    .senderId(msg.getSenderId())
                    .senderName(senderName)
                    .content(msg.getContent())
                    .status(msg.getStatus())
                    .createdAt(msg.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public int markAsRead(UUID userConversationId, UUID userId) {
        UserConversation uc = userConversationRepository.findById(userConversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
        
        if (!uc.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        
        int updated = messageRepository.markConversationMessagesAsRead(uc.getConversationId(), userId);
        
        uc.setUnreadCount(0);
        uc.setUpdatedAt(Instant.now());
        userConversationRepository.save(uc);
        
        return updated;
    }
    
    @Override
    @Transactional
    public void deleteConversation(UUID userConversationId, UUID userId) {
        int deleted = userConversationRepository.softDeleteByIdAndUserId(userConversationId, userId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada");
        }
    }
    
    public long countUnreadForUser(UUID userId) {
        return userConversationRepository.findActiveByUserId(userId).stream()
                .mapToInt(UserConversation::getUnreadCount)
                .sum();
    }
    
    public long countUnreadByConversation(UUID userConversationId, UUID userId) {
        UserConversation uc = userConversationRepository.findById(userConversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada"));
        
        if (!uc.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        
        return uc.getUnreadCount();
    }
}