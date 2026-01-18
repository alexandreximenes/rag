package com.ia.poc_rag.loader;

import com.ia.poc_rag.config.RhProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RHDataLoader {

    private final VectorStore vectorStore;
    private final Resource policyFile;
    private final RhProperties rhProperties;

    public RHDataLoader(VectorStore vectorStore,
                        @Value("classpath:./pdf/Eazybytes_HR_Policies.pdf") Resource policyFile, RhProperties rhProperties) {
        this.vectorStore = vectorStore;
        this.policyFile = policyFile;
        this.rhProperties = rhProperties;
    }

    @PostConstruct
    public void loadSentencesIntoVectorStore() {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(policyFile);
        List<Document> documents = tikaDocumentReader.get();
        TextSplitter textSplitter =
                TokenTextSplitter.builder()
                        .withChunkSize(rhProperties.rh().chunkSize())
                        .withMaxNumChunks(rhProperties.rh().maxChunkSize())
                        .build();
        vectorStore.add(textSplitter.split(documents));
    };
}
