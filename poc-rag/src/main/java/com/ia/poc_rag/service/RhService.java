package com.ia.poc_rag.service;

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
public class RhService {

    static Logger log = LoggerFactory.getLogger(RhService.class);

    private final ChatClient chatClient;
    private final Resource rhSystemPromptTemplate;
    public static final String DOCUMENTS = "documents";
    private final VectorStore vectorStore;

    public RhService(
            @Qualifier("rhChatMemoryClient") ChatClient chatClient, @Value("classpath:/promptTemplates/rhSystemPromptTemplate.st") Resource rhSystemPromptTemplate, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.rhSystemPromptTemplate = rhSystemPromptTemplate;
        this.vectorStore = vectorStore;
    }

    public Flux<String> chat(String message, String username) {
        SearchRequest searchRequest = SearchRequest.builder().query(message).similarityThreshold(0.25).topK(5).build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        if (documents.isEmpty()) {
            return Flux.just("Não encontrei informações relevantes para " +
                    "responder com base no conhecimento disponível.");
        }else{
            log.info("Documentos encontrados: {}", documents.size());
            documents.stream().forEach(document -> {
                log.info("Score: {}", document.getScore());
                log.info("Text: {}", document.getText());
            });
        }
        String similarityText = documents.stream()
                .filter(Objects::nonNull)
                .map(Document::getText)
                .filter(Objects::nonNull)
                .map(this::sanitize)
                .collect(Collectors.joining("\n---\n"));

        return this.chatClient
                .prompt()
                .system(promptSystemSpec -> promptSystemSpec
                        .text(rhSystemPromptTemplate)
                        .param(DOCUMENTS, similarityText))
                .advisors(advisorSpec ->
                        advisorSpec.param(CONVERSATION_ID, username))
                .user(message)
                .stream()
                .content();
    }

    private String sanitize(String texts) {
        return texts.trim()
                .replaceAll("\\s+", " ");
    }
}