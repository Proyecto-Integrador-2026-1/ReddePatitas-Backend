package com.redpatitas.redPatitas.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ConversationsResponse {
    private List<ConversationResponseDto> conversations;
    private long totalUnread;
}