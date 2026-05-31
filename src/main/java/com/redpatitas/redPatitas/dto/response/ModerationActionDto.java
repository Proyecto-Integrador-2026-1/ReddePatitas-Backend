package com.redpatitas.redPatitas.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ModerationActionDto(
        UUID id,
        String tipoAccion,
        String tipoObjetivo,
        UUID idObjetivo,
        UUID realizadoPor,
        String realizadoPorNombre,
        String motivo,
        Instant creadoEn
) {
}
