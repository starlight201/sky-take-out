package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 关联 recommend_session.id。
     */
    private Long sessionId;

    /**
     * 推荐菜品 id。
     */
    private Long dishId;

    /**
     * 推荐套餐 id。当前推荐主要面向菜品，保留该字段用于后续扩展套餐推荐。
     */
    private Long setmealId;

    /**
     * 推荐分数。
     */
    private BigDecimal score;

    /**
     * 推荐理由。
     */
    private String reason;

    /**
     * 推荐排名，从 1 开始。
     */
    private Integer rankNo;

    /**
     * 结果创建时间。
     */
    private LocalDateTime createTime;
}
