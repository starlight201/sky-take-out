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
public class AgentToolCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sessionId;
    private String toolName;
    private String requestParams;
    private String responseSummary;
    private Integer success;
    private Integer costMs;
    private LocalDateTime createTime;
}
