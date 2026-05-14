package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecommendDishDTO implements Serializable {

    /**
     * 菜品 id，对应 dish.id。
     */
    private Long dishId;

    /**
     * 菜品名称。
     */
    private String name;

    /**
     * 分类 id。
     */
    private Long categoryId;

    /**
     * 分类名称。
     */
    private String categoryName;

    /**
     * 菜品价格。
     */
    private BigDecimal price;

    /**
     * 菜品图片地址。
     */
    private String image;

    /**
     * 菜品描述。
     */
    private String description;

    /**
     * 启售状态，1 表示启售，0 表示停售。
     */
    private Integer status;

    /**
     * 近期销量统计，用于热度评分。
     */
    private Integer salesCount;

    /**
     * 推荐综合分，由后端打分算法计算。
     */
    private BigDecimal score;

    /**
     * 后端生成的推荐理由，即使大模型不可用也可以展示。
     */
    private String reason;

    /**
     * 菜品标签集合，例如 微辣、下饭、高蛋白。
     */
    private List<String> tags = new ArrayList<>();
}
