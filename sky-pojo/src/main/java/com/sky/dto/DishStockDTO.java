package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishStockDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dishId;
    private Integer stock;
}
