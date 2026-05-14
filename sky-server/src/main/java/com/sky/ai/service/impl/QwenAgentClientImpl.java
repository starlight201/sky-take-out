package com.sky.ai.service.impl;

import com.sky.ai.service.QwenAgentClient;
import com.sky.properties.QwenProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QwenAgentClientImpl implements QwenAgentClient {

    @Autowired
    private QwenProperties qwenProperties;

    @Override
    public String complete(String prompt) {
        /*
         * 千问 DashScope 提供 OpenAI 兼容模式，所以这里使用 LangChain4j 的
         * OpenAiChatModel 作为统一模型抽象。后续如果要换其他大模型，只需要替换
         * 模型构建部分，不影响上层 Agent 推荐流程。
         */
        if (!qwenProperties.isEnabled()) {
            throw new IllegalStateException("Qwen agent is disabled");
        }
        // API Key 从环境变量 DASHSCOPE_API_KEY 注入，避免把密钥硬编码进配置文件。
        if (qwenProperties.getApiKey() == null || qwenProperties.getApiKey().trim().length() == 0) {
            throw new IllegalStateException("DASHSCOPE_API_KEY is not configured");
        }

        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(qwenProperties.getBaseUrl())
                .apiKey(qwenProperties.getApiKey())
                .modelName(qwenProperties.getModel())
                .build();

        String answer = model.chat(prompt);
        // 防御式校验，避免模型服务返回空内容时业务层拿到空回答。
        if (answer == null || answer.trim().length() == 0) {
            throw new IllegalStateException("Qwen returned empty answer");
        }
        return answer;
    }
}
