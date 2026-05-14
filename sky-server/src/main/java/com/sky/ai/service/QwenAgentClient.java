package com.sky.ai.service;

public interface QwenAgentClient {

    /**
     * 调用千问模型生成回答。
     *
     * @param prompt 已封装好的完整提示词
     * @return 模型输出文本
     */
    String complete(String prompt);
}
