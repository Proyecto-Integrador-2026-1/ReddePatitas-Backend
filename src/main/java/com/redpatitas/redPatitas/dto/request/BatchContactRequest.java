package com.redpatitas.redPatitas.dto.request;

import java.util.List;
import java.util.UUID;

public record BatchContactRequest(List<UUID> userIds) {}