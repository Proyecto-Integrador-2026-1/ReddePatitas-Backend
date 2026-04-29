package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.request.ReportPublicationRequestDto;
import java.util.UUID;

public interface ReportPublicationService {
    String submitReport(ReportPublicationRequestDto dto, UUID reporterUserId);
}
