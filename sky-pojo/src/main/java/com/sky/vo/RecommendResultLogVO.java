package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecommendResultLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sessionId;
    private Long dishId;
    private Long setmealId;
    private String itemName;
    private BigDecimal score;
    private String reason;
    private Integer rankNo;
    private LocalDateTime createTime;
}
