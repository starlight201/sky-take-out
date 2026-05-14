package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishStockAdjustDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dishId;
    private Integer quantity;
}
