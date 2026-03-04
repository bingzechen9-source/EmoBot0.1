package com.emobot.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {
    private String conversationId = "default";
    @NotBlank(message = "消息不能为空")
    private String message;

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
