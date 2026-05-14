package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishTagPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int page;
    private int pageSize;
    private Long dishId;
    private String tagName;
    private String tagType;
}
