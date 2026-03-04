package com.emobot.config;

import com.emobot.memory.FileSystemChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AiConfig {

    @Value("${emobot.memory.storage-path:./data/chat-memory}")
    private String memoryStoragePath;

    @Value("${emobot.rag.enabled:false}")
    private boolean ragEnabled;

    @Value("${emobot.rag.top-k:4}")
    private int ragTopK;

    @Value("${emobot.rag.similarity-threshold:0.6}")
    private double similarityThreshold;

    @Bean
    public FileSystemChatMemoryRepository fileSystemChatMemoryRepository() throws IOException {
        Path path = Paths.get(memoryStoragePath);
        Files.createDirectories(path);
        return new FileSystemChatMemoryRepository(path);
    }

    @Bean
    public MessageWindowChatMemory chatMemory(FileSystemChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor chatMemoryAdvisor(MessageWindowChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    @Primary
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel,
                                                 MessageWindowChatMemory chatMemory,
                                                 org.springframework.beans.factory.ObjectProvider<VectorStore> vectorStoreProvider) {
        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        var builder = ChatClient.builder(chatModel);
        if (ragEnabled) {
            VectorStore vs = vectorStoreProvider.getIfAvailable();
            if (vs != null) {
                var qaAdvisor = org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.builder(vs)
                        .searchRequest(org.springframework.ai.vectorstore.SearchRequest.builder()
                                .topK(ragTopK)
                                .similarityThreshold(similarityThreshold)
                                .build())
                        .build();
                builder.defaultAdvisors(memoryAdvisor, qaAdvisor);
            } else {
                builder.defaultAdvisors(memoryAdvisor);
            }
        } else {
            builder.defaultAdvisors(memoryAdvisor);
        }
        return builder;
    }
}
