package com.redpatitas.redPatitas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SendMessageRequestDto {
    @NotNull
    private UUID reportId;
    
    @NotNull
    private UUID receiverId; 
    
    @NotBlank
    private String content;
}