package com.sky.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AgentRecentOrderRawVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dishId;
    private Long setmealId;
    private String dishName;
    private String category;
    private String tagNames;
    private Integer count;
}
