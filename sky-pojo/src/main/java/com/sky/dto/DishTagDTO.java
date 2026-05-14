package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DishTagDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long dishId;
    private String tagName;
    private String tagType;
    private BigDecimal weight;
}
