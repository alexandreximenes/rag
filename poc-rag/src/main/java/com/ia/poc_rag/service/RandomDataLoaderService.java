package com.ia.poc_rag.service;

import com.ia.poc_rag.config.properties.DataLoaderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class RandomDataLoaderService {

    static Logger log = LoggerFactory.getLogger(RandomDataLoaderService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Resource systemPromptRandomDataTemplate;
    private final DataLoaderProperties properties;
    public static final String DOCUMENTS = "documents";

    public RandomDataLoaderService(
            @Qualifier("chatMemoryClient") ChatClient chatClient,
            VectorStore vectorStore,
            @Value("classpath:/promptTemplates/systemPromptRandomDataTemplate.st")
            Resource systemPromptRandomDataTemplate,
            DataLoaderProperties properties) {

        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.systemPromptRandomDataTemplate = systemPromptRandomDataTemplate;
        this.properties = properties;
    }

    public Flux<String> chat(String message, String username) {
        SearchRequest searchRequest = getSearchRequest(message);
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        if (documents.isEmpty()) {
            return Flux.just("Não encontrei informações relevantes para " +
                    "responder com base no conhecimento disponível.");
        }
        String similarContext = getSimilarContext(documents);
        return getStreamContent(message, username, similarContext);
    }

    private SearchRequest getSearchRequest(String message) {
        return SearchRequest.builder()
                .query(message)
                .topK(properties.topK())
                .similarityThreshold(properties.similarityThreshold())
                .build();
    }

    private Flux<String> getStreamContent(String message, String username, String similarContext) {
        return this.chatClient
                .prompt()
                .system(promptSystemSpec -> {
                    promptSystemSpec.text(systemPromptRandomDataTemplate)
                            .param(DOCUMENTS, similarContext);
                })
                .advisors(advisorSpec ->
                        advisorSpec.param(CONVERSATION_ID, username))
                .user(message)
                .stream()
                .content();
    }

    private String getSimilarContext(List<Document> documents) {
        return documents.stream()
                .filter(Objects::nonNull)
                .peek(document -> {
                    log.debug("Similar Score: {}", document.getScore());
                    log.debug("Similar Metadata Document: {}", document.getMetadata());
                })
                .map(Document::getText)
                .filter(Objects::nonNull)
                .peek(d -> log.info("Similar Text Document: {}\n", d))
                .map(this::sanitize)
                .collect(Collectors.joining("\n---\n"));
    }

    private String sanitize(String text) {
        return text.length() > 1500
                ? text.substring(0, 1500)
                : text;
    }
}