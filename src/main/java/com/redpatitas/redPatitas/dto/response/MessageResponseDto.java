package com.redpatitas.redPatitas.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MessageResponseDto {
    private UUID id;
    private UUID senderId;
    private String senderName;  // desde auth service
    private String content;
    private String status;
    private Instant createdAt;
}