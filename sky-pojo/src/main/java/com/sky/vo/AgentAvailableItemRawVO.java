package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AgentAvailableItemRawVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dishId;
    private Long setmealId;
    private String itemType;
    private String name;
    private BigDecimal price;
    private Integer status;
    private String category;
    private Integer categoryStatus;
    private String tagNames;
    private Integer salesCount;
    private String image;
    private String description;
}
