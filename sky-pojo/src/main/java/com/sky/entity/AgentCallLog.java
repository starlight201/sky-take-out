package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户 id。
     */
    private Long userId;

    /**
     * 调用的模型名称，例如 qwen-plus。
     */
    private String modelName;

    /**
     * 用户原始问题。
     */
    private String userQuery;

    /**
     * 实际发送给大模型的 Prompt。
     */
    private String prompt;

    /**
     * 大模型返回内容，或本地降级回答。
     */
    private String response;

    /**
     * 调用是否成功，1 成功，0 失败。
     */
    private Integer success;

    /**
     * 调用失败时的错误信息。
     */
    private String errorMessage;

    /**
     * 调用耗时，单位毫秒。
     */
    private Long latencyMs;

    /**
     * 日志创建时间。
     */
    private LocalDateTime createTime;
}
