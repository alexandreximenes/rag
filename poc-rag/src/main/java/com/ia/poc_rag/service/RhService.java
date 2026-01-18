package com.ia.poc_rag.service;

import com.ia.poc_rag.config.RhProperties;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class RhService {

    static Logger log = LoggerFactory.getLogger(RhService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Resource rhSystemPromptTemplate;
    private final RhProperties rhProperties;
    public static final String DOCUMENTS = "documents";

    public RhService(
            @Qualifier("chatMemoryClient") ChatClient chatClient,
            VectorStore vectorStore,
            @Value("classpath:/promptTemplates/rhSystemPromptTemplate.st")
            Resource rhSystemPromptTemplate,
            RhProperties rhProperties) {

        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.rhSystemPromptTemplate = rhSystemPromptTemplate;
        this.rhProperties = rhProperties;
    }

    public Flux<String> rhChat(String message, String username) {
        SearchRequest searchRequest = getSearchRequest(message);
        List<Document> vectorDocuments = vectorStore.similaritySearch(searchRequest);
        Set<Document> document = removeDuplicatesAndSelectHighScore(vectorDocuments);
        if (document.isEmpty()) {
            return Flux.just("Não encontrei informações relevantes para responder com base no conhecimento disponível.");
        }
        String similarContext = getSimilarContext(document);
        return getStreamContent(message, username, similarContext);
    }

    private Set<Document> removeDuplicatesAndSelectHighScore(List<Document> documents) {
        return documents.stream().filter(doc -> doc.getText() != null)
                .collect(Collectors.toMap(
                        Document::getText,
                        doc -> doc,
                        (d1, d2) -> d1.getScore() >= d2.getScore() ? d1 : d2
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(Document::getScore).reversed())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private SearchRequest getSearchRequest(String message) {
        return SearchRequest.builder()
                .query(message)
                // Busca os 3 documentos mais similares (relevantes) ao prompt do usuário
                .topK(rhProperties.rh().topK())
                // Busca somente documentos que contenham pelo menos
                // 50% de similaridade com o prompt do usuário
                .similarityThreshold(rhProperties.rh().similarityThreshold())
                .build();
    }

    private Flux<String> getStreamContent(String message, String username, String similarContext) {
        return this.chatClient
                .prompt()
                .system(promptSystemSpec -> {
                    promptSystemSpec.text(rhSystemPromptTemplate)
                            .param(DOCUMENTS, similarContext);
                })
                .advisors(advisorSpec ->
                        advisorSpec.param(CONVERSATION_ID, username))
                .user(message)
                .stream()
                .content();
    }

    private String getSimilarContext(Set<Document> documents) {
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