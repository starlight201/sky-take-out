package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RecommendLogItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dishId;
    private Long setmealId;
    private BigDecimal score;
    private String reason;
}
