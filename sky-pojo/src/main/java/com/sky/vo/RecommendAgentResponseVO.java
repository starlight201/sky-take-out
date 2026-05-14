package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class RecommendAgentResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String query;
    private String answer;
    private Map<String, Object> intent;
    private List<RecommendAgentItemVO> items;
}
