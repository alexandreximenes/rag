package com.ia.poc_rag.config;

import com.ia.poc_rag.config.advisor.TokenUsageAuditAdvisor;
import com.ia.poc_rag.config.properties.RhProperties;
import com.ia.poc_rag.service.PIIMaskingDocumentPostProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RHChatMemoryConfig {

    public static final String ENGLISH = "english";
    private final RhProperties properties;
    private final VectorStore vectorStore;

    public RHChatMemoryConfig(RhProperties properties, VectorStore vectorStore) {
        this.properties = properties;
        this.vectorStore = vectorStore;
    }

    @Bean("rhChatMemoryClient")
    public ChatClient rhChatMemoryClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor) {
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(List.of(
                        new SimpleLoggerAdvisor(),
                        memoryAdvisor,
                        new TokenUsageAuditAdvisor(),
                        retrievalAugmentationAdvisor
                ))
                .build();
    }

    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(ChatClient.Builder builder){
        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(TranslationQueryTransformer.builder()
                        .chatClientBuilder(builder.clone())
                        .targetLanguage(ENGLISH)
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(properties.similarityThreshold())
                        .topK(properties.topK())
                        .build())
                .documentPostProcessors(PIIMaskingDocumentPostProcessor.builder())
                .build();
    }
}
