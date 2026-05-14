package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentShopStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer status;
    private String message;
}
