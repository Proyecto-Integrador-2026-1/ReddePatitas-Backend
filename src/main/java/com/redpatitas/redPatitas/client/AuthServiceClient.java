package com.redpatitas.redPatitas.client;

import com.redpatitas.redPatitas.dto.request.BatchContactRequest;
import com.redpatitas.redPatitas.dto.response.BatchContactResponse;
import com.redpatitas.redPatitas.dto.response.ContactInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestClient authRestClient;

    public ContactInfoResponse getContactInfo(UUID userId) {
        return authRestClient.get()
                .uri("/api/v1/users/internal/{userId}/contact", userId)
                .retrieve()
                .body(ContactInfoResponse.class);
    }

    public Map<UUID, ContactInfoResponse> getBatchContactInfo(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        BatchContactResponse response = authRestClient.post()
                .uri("/api/v1/users/internal/batch/contact")
                .body(new BatchContactRequest(userIds))
                .retrieve()
                .body(BatchContactResponse.class);
        return response != null ? response.users() : Map.of();
    }
}