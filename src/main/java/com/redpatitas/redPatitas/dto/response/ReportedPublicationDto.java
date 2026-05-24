package com.redpatitas.redPatitas.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReportedPublicationDto(
        UUID reportId,
        UUID userId,
        UUID petId,
        String petName,
        String tipoReporte,
        Instant fechaCreacion,
        long reportCount,
        Instant lastReportAt,
        String imagenUrl,
        String thumbnailUrl,
        String publisherName,
        String reporterName,
        String reporterReason
) {
}
