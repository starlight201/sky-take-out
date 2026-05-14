package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RecommendSessionLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String userQuery;
    private String parsedIntent;
    private Integer resultCount;
    private LocalDateTime createTime;
}
