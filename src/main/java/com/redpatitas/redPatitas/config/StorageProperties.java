package com.redpatitas.redPatitas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        String mode,
        String publicUrl
) {
}
