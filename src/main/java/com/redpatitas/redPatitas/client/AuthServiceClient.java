package com.redpatitas.redPatitas.client;

import com.redpatitas.redPatitas.dto.request.BatchContactRequest;
import com.redpatitas.redPatitas.dto.response.BatchContactResponse;
import com.redpatitas.redPatitas.dto.response.ContactInfoResponse;
import com.redpatitas.redPatitas.dto.response.UserMetricsResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.UUID;



@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestClient authRestClient;
    @Value("${auth.service.api-key}")
    private String internalApiKey;

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

    public UserMetricsResponse getUserMetrics() {
        return authRestClient.get()
                .uri("/api/v1/admin/users/internal/metrics")
                .header("X-Internal-API-Key", internalApiKey)
                .retrieve()
                .toEntity(UserMetricsResponse.class)
                .getBody();
    }

    public void blockUser(UUID userId) {
        authRestClient.post()
                .uri("/api/v1/admin/users/internal/{userId}/block", userId)
                .header("X-Internal-API-Key", internalApiKey)
                .retrieve()
                .toBodilessEntity();
    }

    public void unblockUser(UUID userId) {
        authRestClient.post()
                .uri("/api/v1/admin/users/internal/{userId}/unblock", userId)
                .header("X-Internal-API-Key", internalApiKey)
                .retrieve()
                .toBodilessEntity();
    }

    public void deactivateUser(UUID userId) {
        authRestClient.post()
                .uri("/api/v1/admin/users/internal/{userId}/deactivate", userId)
                .header("X-Internal-API-Key", internalApiKey)
                .retrieve()
                .toBodilessEntity();
    }

    public void activateUser(UUID userId) {
        authRestClient.post()
                .uri("/api/v1/admin/users/internal/{userId}/activate", userId)
                .header("X-Internal-API-Key", internalApiKey)
                .retrieve()
                .toBodilessEntity();
    }
}