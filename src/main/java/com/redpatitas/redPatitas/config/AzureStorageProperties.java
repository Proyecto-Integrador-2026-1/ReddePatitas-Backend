package com.redpatitas.redPatitas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.storage")
public record AzureStorageProperties(
        String connectionString,
        String containerName,
        String thumbnailContainerName
) {
}
