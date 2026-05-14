package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFoodPreference implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String preferenceType;
    private String preferenceValue;
    private String source;
    private BigDecimal weight;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
