package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.dto.response.ContactResponseDto;
import com.redpatitas.redPatitas.entity.Pet;
import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.repository.PetRepository;
import com.redpatitas.redPatitas.repository.ReportRepository;
import com.redpatitas.redPatitas.service.interfaces.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class ContactServiceImpl implements ContactService {
    private final ReportRepository reportRepository;
    private final PetRepository petRepository;

    public ContactServiceImpl(ReportRepository reportRepository, PetRepository petRepository) {
        this.reportRepository = reportRepository;
        this.petRepository = petRepository;
    }

    @Override
    public ContactResponseDto getContactByReportId(UUID reportId, UUID requesterId) {
        Optional<Report> maybe = reportRepository.findById(reportId);
        if (maybe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado");
        }
        Report report = maybe.get();
        UUID ownerId = report.getUserId();
        if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicador no establecido para este reporte");
        }
        if (ownerId.equals(requesterId)) {
            return new ContactResponseDto(ownerId, "Esta es tu publicación");
        }
        // Return only the ownerId; frontend will query auth service for profile
        return new ContactResponseDto(ownerId, null);
    }

    @Override
    public ContactResponseDto getContactByPetId(UUID petId, UUID requesterId) {
        Optional<Pet> maybe = petRepository.findById(petId);
        if (maybe.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada");
        }
        Pet pet = maybe.get();
        UUID ownerId = pet.getUserId();
        if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicador no establecido para esta mascota");
        }
        if (ownerId.equals(requesterId)) {
            return new ContactResponseDto(ownerId, "Esta es tu publicación");
        }
        return new ContactResponseDto(ownerId, null);
    }
}
