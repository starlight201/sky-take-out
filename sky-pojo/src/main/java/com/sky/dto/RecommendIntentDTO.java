package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecommendIntentDTO implements Serializable {

    /**
     * 口味标签，例如 辣、微辣、清淡、酸甜。
     */
    private List<String> tasteTags = new ArrayList<>();

    /**
     * 营养标签，例如 高蛋白、低脂、少油。
     */
    private List<String> nutritionTags = new ArrayList<>();

    /**
     * 场景标签，例如 适合午餐、下饭、家常。
     */
    private List<String> sceneTags = new ArrayList<>();

    /**
     * 需要避开的标签，例如 香菜、牛肉、花生。
     */
    private List<String> excludeTags = new ArrayList<>();

    /**
     * 用户预算上限，单位：元。
     */
    private BigDecimal budget;

    /**
     * 用餐时段，例如 午餐、晚餐。
     */
    private String mealType;

    /**
     * 综合场景描述，例如 健身餐、减脂餐。
     */
    private String scene;
}
