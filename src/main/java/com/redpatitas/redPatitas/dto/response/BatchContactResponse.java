package com.redpatitas.redPatitas.dto.response;

import java.util.Map;
import java.util.UUID;

public record BatchContactResponse(Map<UUID, ContactInfoResponse> users) {}