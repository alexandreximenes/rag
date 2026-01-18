package com.ia.poc_rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public record DataLoaderProperties(
        DataLoader dataLoader
) {

    public record DataLoader(
            double similarityThreshold,
            int topK,
            int chunkSize,
            int maxChunkSize
    ) {}
}
