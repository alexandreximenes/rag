package com.ia.poc_rag.config;

import com.ia.poc_rag.config.advisor.TokenUsageAuditAdvisor;
import com.ia.poc_rag.config.properties.RhProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RHChatMemoryConfig {

    private final RhProperties rhProperties;

    public RHChatMemoryConfig(RhProperties rhProperties) {
        this.rhProperties = rhProperties;
    }

    @Bean("rhChatMemoryClient")
    public ChatClient rhChatMemoryClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory) {
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(), memoryAdvisor, new TokenUsageAuditAdvisor()))
                .build();
    }

//    @Bean
//    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore) {
//
//        return RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(VectorStoreDocumentRetriever.builder()
//                        .vectorStore(vectorStore)
//                        .topK(rhProperties.topK())
//                        .similarityThreshold(rhProperties.similarityThreshold())
//                        .build())
//                .build();
//    }
}
