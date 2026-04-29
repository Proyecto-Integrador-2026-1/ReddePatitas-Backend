package com.redpatitas.redPatitas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private UUID mensajeId;
    private UUID conversacionId;
    private UUID remitenteId;
    private String contenido;
    private String estado;
    private Instant creadoEn;
}
