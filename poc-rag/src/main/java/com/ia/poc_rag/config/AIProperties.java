package com.ia.poc_rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public record AIProperties(
        AI ai
) {

    public record AI(
            double similarityThreshold,
            int topK,
            int chunkSize,
            int maxChunkSize
    ) {}
}
