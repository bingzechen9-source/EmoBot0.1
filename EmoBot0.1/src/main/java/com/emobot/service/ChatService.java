package com.emobot.service;

import com.emobot.mcp.EmoBotMcpTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            你是 EmoBot，一位专业的情感指导助手。你的职责是：
            1. 倾听用户的情绪与困扰，给予共情与理解；
            2. 在适当时机可调用工具：搜索图片、查地图、记录情绪、呼吸练习、冥想推荐；
            3. 提供情感支持与自我关怀建议，但不对严重心理问题做诊断或替代专业治疗；
            4. 回复温暖、简洁，避免说教。
            """;

    private final ChatClient chatClient;
    private final MessageWindowChatMemory chatMemory;
    private final EmoBotMcpTools emoBotTools;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       MessageWindowChatMemory chatMemory,
                       EmoBotMcpTools emoBotTools) {
        this.chatMemory = chatMemory;
        this.emoBotTools = emoBotTools;
        this.chatClient = chatClientBuilder.build();
    }

    public String chat(String conversationId, String userMessage) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(emoBotTools)
                .user(userMessage)
                .call()
                .content();
    }

    public void clearMemory(String conversationId) {
        chatMemory.clear(conversationId);
    }
}
