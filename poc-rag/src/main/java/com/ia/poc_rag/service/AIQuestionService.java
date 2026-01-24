package com.ia.poc_rag.service;

import com.ia.poc_rag.config.properties.AIProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class AIQuestionService {

    static Logger log = LoggerFactory.getLogger(AIQuestionService.class);

    private final ChatClient chatClient;
    private final ChatClient webSearchChatClient;
    private final VectorStore vectorStore;
    private final Resource iaSystemPromptTemplate;
    private final AIProperties aiProperties;
    public static final String DOCUMENTS = "documents";

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize dataSize;

    public AIQuestionService(
            @Qualifier("chatMemoryClient") ChatClient chatClient,
            @Qualifier("webSearchRAGChatClient") ChatClient webSearchChatClient,
            VectorStore vectorStore,
            @Value("classpath:/promptTemplates/springIASystemPromptTemplate.st")
            Resource iaSystemPromptTemplate,
            AIProperties aiProperties) {

        this.chatClient = chatClient;
        this.webSearchChatClient = webSearchChatClient;
        this.vectorStore = vectorStore;
        this.iaSystemPromptTemplate = iaSystemPromptTemplate;
        this.aiProperties = aiProperties;
    }

    public Flux<String> chat(String message, String username) {
        SearchRequest searchRequest = getSearchRequest(message);
        List<Document> vectorDocuments = vectorStore.similaritySearch(searchRequest);
        Set<Document> document = removeDuplicatesAndSelectHighScore(vectorDocuments);
        if (document.isEmpty()) {
            return Flux.just("Não encontrei informações relevantes para responder com base no conhecimento disponível.");
        }
        String similarContext = buildSimilarContext(document);
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
                // Busca somente documentos que contenham pelo menos
                .topK(aiProperties.topK())
                // 50% de similaridade com o prompt do usuário
                .similarityThreshold(aiProperties.similarityThreshold())
                .build();
    }

    private Flux<String> getStreamContent(String message, String username, String similarContext) {
        return this.chatClient
                .prompt()
                .system(promptSystemSpec -> {
                    promptSystemSpec.text(iaSystemPromptTemplate)
                            .param(DOCUMENTS, similarContext);
                })
                .options(ChatOptions.builder().temperature(0.7).build())
                .advisors(advisorSpec ->
                        advisorSpec.param(CONVERSATION_ID, username))
                .user(message)
                .stream()
                .content();
    }

    private String buildSimilarContext(Set<Document> documents) {
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

    private String sanitize(String texts) {
        return texts.trim()
                .replaceAll("\\s+", " ");
    }

    public void uploadFile(LinkedHashSet<MultipartFile> files) {
        validatePdf(files);

        files.forEach(file -> {
            TikaDocumentReader documentReader = null;
            documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = documentReader.get();
            enrichMetadata(documents, file);
            List<Document> chunks = split(documents);
            vectorStore.add(chunks);
        });
    }

    private List<Document> split(List<Document> documents) {
        TextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(aiProperties.chunkSize())
                .withMaxNumChunks(aiProperties.maxChunkSize())
                .build();

        return splitter.split(documents);
    }

    private void enrichMetadata(List<Document> documents, MultipartFile file) {
        documents.forEach(doc -> {
            doc.getMetadata().put("source", file.getOriginalFilename());
            doc.getMetadata().put("content_type", file.getContentType());
            doc.getMetadata().put("domain", "spring-ai");
        });
    }

    private void validatePdf(LinkedHashSet<MultipartFile> files) {

        files.forEach(file ->{
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Arquivo não informado ou vazio");
            }

            if (file.getSize() > dataSize.toBytes()) {
                throw new IllegalArgumentException("Arquivo excede o tamanho máximo permitido");
            }

            if (!MediaType.APPLICATION_PDF_VALUE.equals(file.getContentType())) {
                throw new IllegalArgumentException("Apenas arquivos PDF são permitidos");
            }
        });
    }

    public Flux<String> webSearchChat(String message, String username) {
        return webSearchChatClient.prompt()
                .advisors(advisorSpec ->
                        advisorSpec.param(CONVERSATION_ID, username))
                .user(message)
                .stream()
                .content();
    }
}