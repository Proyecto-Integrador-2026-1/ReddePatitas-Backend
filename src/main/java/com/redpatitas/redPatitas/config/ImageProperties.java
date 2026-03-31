package com.redpatitas.redPatitas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "image")
public record ImageProperties(
        long maxSizeBytes,
        Thumbnail thumbnail
) {
    public record Thumbnail(
            int width,
            int height
    ) {
    }
}
