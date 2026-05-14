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
public class UserFoodPreference implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户 id。
     */
    private Long userId;

    /**
     * 偏好类型：like 喜欢，dislike 不喜欢，allergy 过敏，taste 口味偏好。
     */
    private String preferenceType;

    /**
     * 偏好值，例如 辣、牛肉、花生、清淡。
     */
    private String preferenceValue;

    /**
     * 偏好来源：manual 手动维护，后续可扩展为 order/infer。
     */
    private String source;

    /**
     * 偏好强度，数值越高对推荐分数影响越大。
     */
    private BigDecimal weight;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
