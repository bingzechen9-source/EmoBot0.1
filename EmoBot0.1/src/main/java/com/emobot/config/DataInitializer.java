package com.emobot.config;

import com.emobot.service.RagKnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @ConditionalOnProperty(name = "emobot.rag.enabled", havingValue = "true")
    @ConditionalOnBean(RagKnowledgeService.class)
    public ApplicationRunner initRagKnowledge(RagKnowledgeService ragKnowledgeService) {
        return args -> {
            try {
                ragKnowledgeService.loadFromText(
                    "情绪管理建议：当感到焦虑时，可以尝试478呼吸法：吸气4秒、屏息7秒、呼气8秒，重复3-5轮。正念冥想有助于减轻压力，建议每天练习10-15分钟。",
                    "emotion-management");
                ragKnowledgeService.loadFromText(
                    "情感支持原则：倾听比建议更重要。表达共情时可以说我理解你的感受。自我关怀包括：接纳自己的情绪、适度休息、保持规律作息。",
                    "emotion-support");
                log.info("RAG 知识库初始化完成");
            } catch (Exception e) {
                log.warn("RAG 知识库初始化跳过: {}", e.getMessage());
            }
        };
    }
}
