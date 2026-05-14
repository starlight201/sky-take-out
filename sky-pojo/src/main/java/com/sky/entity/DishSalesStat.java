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
    private Long dishId;
    private Integer salesCount;
    private LocalDate statDate;
    private String statType;
}
