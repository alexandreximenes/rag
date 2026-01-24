package com.ia.poc_rag.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tavily")
public record TavilyProperties(
        String apiKey,
        String baseUrl
) { }
