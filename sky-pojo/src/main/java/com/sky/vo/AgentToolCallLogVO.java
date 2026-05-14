package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AgentToolCallLogVO implements Serializable {

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
