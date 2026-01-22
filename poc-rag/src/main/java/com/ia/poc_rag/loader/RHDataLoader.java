package com.ia.poc_rag.loader;

import com.ia.poc_rag.config.properties.RhProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.rh", name = "loader-file-enabled", havingValue = "true", matchIfMissing = false)
public class RHDataLoader {

    private final VectorStore vectorStore;
    private final Resource policyFile;
    private final RhProperties properties;

    private static final Logger log = LoggerFactory.getLogger(RHDataLoader.class);
    public RHDataLoader(VectorStore vectorStore,
                        @Value("classpath:./pdf/Eazybytes_HR_Policies.pdf") Resource policyFile,
                        RhProperties properties) {
        this.vectorStore = vectorStore;
        this.policyFile = policyFile;
        this.properties = properties;
    }

    @PostConstruct
    public void loadSentencesIntoVectorStore() {
        log.info("Carregando Random Data Loader no Vector Store...");

        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(policyFile);
        List<Document> documents = tikaDocumentReader.get();
        if(!documents.isEmpty() && vectorStore.similaritySearch(getSearchRequest(documents)).isEmpty()) {
            log.info("Carregando {} documento do RH no Vector Store.", documents.size());
            TextSplitter textSplitter = buildTokenTextSplitter();
            vectorStore.add(textSplitter.split(documents));
        } else {
            log.info("Nenhum carregamento necessário. Documento do RH já existe no Vector Store.");
        }
    }

    private TokenTextSplitter buildTokenTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(properties.chunkSize())
                .withMaxNumChunks(properties.maxChunkSize())
                .build();
    }

    private SearchRequest getSearchRequest(List<Document> documents) {
        return SearchRequest.builder().similarityThreshold(0.8).topK(3).query(documents.getFirst().getText().substring(0,500)).build();
    }
}
