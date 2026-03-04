package com.emobot.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnBean(VectorStore.class)
public class RagKnowledgeService {

    private final VectorStore vectorStore;

    public RagKnowledgeService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void loadFromText(String text, String metadata) {
        Document doc = new Document(text);
        doc.getMetadata().put("source", metadata);
        vectorStore.add(List.of(doc));
    }
}
