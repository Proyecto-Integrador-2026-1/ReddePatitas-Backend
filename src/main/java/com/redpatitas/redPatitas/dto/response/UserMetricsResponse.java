package com.redpatitas.redPatitas.dto.response;

public record UserMetricsResponse(
        long totalUsers,
        long totalActive,
        long totalBlocked,
        long totalDeactivated
) {}
