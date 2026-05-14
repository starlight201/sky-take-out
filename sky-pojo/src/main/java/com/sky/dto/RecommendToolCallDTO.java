package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecommendToolCallDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String toolName;
    private String requestParams;
    private String responseSummary;
    private Integer success;
    private Integer costMs;
}
