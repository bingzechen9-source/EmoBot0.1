package com.emobot.controller;

import com.emobot.common.Result;
import com.emobot.controller.dto.ChatRequest;
import com.emobot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "聊天接口", description = "EmoBot 情感指导对话 API")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "发送消息")
    @PostMapping
    public Result<String> chat(@RequestBody ChatRequest request) {
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isBlank()) conversationId = "default";
        String reply = chatService.chat(conversationId, request.getMessage());
        return Result.success(reply);
    }

    @Operation(summary = "清除会话记忆")
    @DeleteMapping("/memory")
    public Result<Void> clearMemory(@RequestParam String conversationId) {
        chatService.clearMemory(conversationId);
        return Result.success();
    }
}
