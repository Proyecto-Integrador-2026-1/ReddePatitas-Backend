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
    private long unreadCount;
    private String ownerName;
    private String user2Name;
    private String publisherName;
}
