package com.redpatitas.redPatitas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponseDto {
    private UUID ownerId;
    private String message; // optional message like "Esta es tu publicación"
}
