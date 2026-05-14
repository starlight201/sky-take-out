package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AgentUserPreferenceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private List<String> likes = new ArrayList<>();
    private List<String> dislikes = new ArrayList<>();
    private List<String> allergies = new ArrayList<>();
    private BigDecimal defaultBudget;
}
