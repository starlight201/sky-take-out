package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecommendLogPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int page;
    private int pageSize;
    private Long userId;
    private String query;
}
