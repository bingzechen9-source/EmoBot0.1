package com.emobot.memory;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FileSystemChatMemoryRepository implements ChatMemoryRepository {

    private final Path storagePath;
    private final ConcurrentHashMap<String, List<Message>> memory = new ConcurrentHashMap<>();

    public FileSystemChatMemoryRepository(Path storagePath) {
        this.storagePath = storagePath;
        loadFromDisk();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return new ArrayList<>(memory.getOrDefault(conversationId, List.of()));
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        memory.put(conversationId, new ArrayList<>(messages));
        persistConversation(conversationId, messages);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        memory.remove(conversationId);
        FileUtil.del(new File(storagePath.toFile(), conversationId + ".json"));
    }

    @Override
    public List<String> findConversationIds() {
        return new ArrayList<>(memory.keySet());
    }

    private void loadFromDisk() {
        File dir = storagePath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String conversationId = file.getName().replace(".json", "");
                List<Message> messages = loadConversation(conversationId);
                if (!messages.isEmpty()) {
                    memory.put(conversationId, messages);
                }
            }
        }
    }

    private List<Message> loadConversation(String conversationId) {
        try {
            File file = new File(storagePath.toFile(), conversationId + ".json");
            if (!file.exists()) return List.of();
            String json = FileUtil.readString(file, StandardCharsets.UTF_8);
            JSONArray arr = JSONUtil.parseArray(json);
            List<Message> result = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                result.add(parseMessage(arr.getJSONObject(i)));
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    private void persistConversation(String conversationId, List<Message> messages) {
        try {
            Files.createDirectories(storagePath);
            JSONArray arr = new JSONArray();
            for (Message msg : messages) {
                arr.add(messageToJson(msg));
            }
            File file = new File(storagePath.toFile(), conversationId + ".json");
            FileUtil.writeString(arr.toStringPretty(), file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("持久化对话失败: " + conversationId, e);
        }
    }

    private JSONObject messageToJson(Message msg) {
        JSONObject o = new JSONObject();
        o.set("type", msg.getMessageType().name());
        o.set("content", msg.getText());
        o.set("metadata", msg.getMetadata());
        return o;
    }

    private Message parseMessage(JSONObject o) {
        String type = o.getStr("type", "USER");
        String content = o.getStr("content", "");
        MessageType mt = MessageType.valueOf(type);
        return switch (mt) {
            case USER -> new UserMessage(content);
            case ASSISTANT -> new AssistantMessage(content);
            case SYSTEM -> new SystemMessage(content);
            default -> new UserMessage(content);
        };
    }
}
