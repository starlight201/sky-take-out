package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AgentRecentOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dishId;
    private Long setmealId;
    private String dishName;
    private String category;
    private List<String> tags;
    private Integer count;
}
