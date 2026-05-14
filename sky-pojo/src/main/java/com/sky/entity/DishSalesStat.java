package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishSalesStat implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 菜品 id。
     */
    private Long dishId;

    /**
     * 统计周期内销量。
     */
    private Integer salesCount;

    /**
     * 统计日期。
     */
    private LocalDate statDate;

    /**
     * 统计类型，例如 daily、weekly。
     */
    private String statType;
}
