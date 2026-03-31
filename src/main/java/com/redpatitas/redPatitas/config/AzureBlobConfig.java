package com.redpatitas.redPatitas.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "storage.mode", havingValue = "azure")
public class AzureBlobConfig {

    @Bean(name = "blobContainerClient")
    public BlobContainerClient blobContainerClient(AzureStorageProperties properties) {
        return new BlobContainerClientBuilder()
                .connectionString(properties.connectionString())
                .containerName(properties.containerName())
                .buildClient();
    }

    @Bean(name = "thumbnailContainerClient")
    public BlobContainerClient thumbnailContainerClient(AzureStorageProperties properties) {
        return new BlobContainerClientBuilder()
                .connectionString(properties.connectionString())
                .containerName(properties.thumbnailContainerName())
                .buildClient();
    }
}
