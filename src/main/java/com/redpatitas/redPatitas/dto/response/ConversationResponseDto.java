package com.redpatitas.redPatitas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponseDto {
    private UUID conversacionId;
    private UUID reportId;
    private UUID ownerId;
    private UUID userId2;
    private Instant creadoEn;

    private long unreadCount; // unread messages for the requesting user in this conversation
}
