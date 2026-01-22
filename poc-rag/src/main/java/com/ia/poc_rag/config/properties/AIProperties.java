package com.ia.poc_rag.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AIProperties(
        double similarityThreshold,
        int topK,
        int chunkSize,
        int maxChunkSize
) { }
