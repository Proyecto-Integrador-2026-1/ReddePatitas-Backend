package com.redpatitas.redPatitas.service.interfaces;

import com.redpatitas.redPatitas.dto.response.ContactResponseDto;

import java.util.UUID;

public interface ContactService {
    ContactResponseDto getContactByReportId(UUID reportId, UUID requesterId);
    ContactResponseDto getContactByPetId(UUID petId, UUID requesterId);
}
