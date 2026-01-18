package com.ia.poc_rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public record RhProperties(
        Rh rh
) {

    public record Rh(
            double similarityThreshold,
            int topK,
            int chunkSize,
            int maxChunkSize
    ) {}
}
