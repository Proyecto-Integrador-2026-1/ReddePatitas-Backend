package com.redpatitas.redPatitas.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SendMessageRequestDto {
    private UUID reportId; // optional if conversacionId provided
    private UUID conversacionId; // optional
    private String contenido;
}
