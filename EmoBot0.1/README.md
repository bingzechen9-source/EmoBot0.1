# EmoBot - 情感指导助手

基于 **Spring Boot 3 + Spring AI + RAG + Tool Calling + MCP** 的 EmoBot，为用户提供情感指导服务。

## 项目特色

- **项目架构**：Spring Boot 3、Hutool、Lombok、SpringDoc 接口文档、全局异常处理器
- **AI 大模型集成**：支持通义千问（OpenAI 兼容接口）、Ollama 本地模型，统一调用接口，灵活切换
- **多轮对话**：基于 MessageChatMemoryAdvisor 与 ChatMemory，保持语境连贯
- **对话记忆持久化**：自研基于文件系统的 ChatMemory，服务重启后记忆不丢失
- **RAG 知识库**：向量检索增强，结合情感指导领域知识
- **MCP / Tool Calling**：图片搜索、地图、情绪记录、呼吸练习、冥想推荐等集成工具，支持 ReAct 模式自主调用

## 快速开始

1. 环境：JDK 21+、Maven 3.8+、Ollama 或 通义千问 API Key
2. 配置：编辑 `application.yml` 中的模型和 API Key
3. 运行：`mvn spring-boot:run`
4. 访问：http://localhost:8080/ （前端） | http://localhost:8080/swagger-ui.html （API 文档）

## 项目位置

本项目位于：**`c:\Users\13061\Desktop\EmoBot0.1`**
