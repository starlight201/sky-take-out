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
public class DishTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 关联菜品 id。
     */
    private Long dishId;

    /**
     * 标签名称，例如 微辣、下饭、高蛋白。
     */
    private String tagName;

    /**
     * 标签类型，例如 taste、scene、nutrition、ingredient。
     */
    private String tagType;

    /**
     * 标签权重，后续可用于增强推荐打分。
     */
    private BigDecimal weight;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
