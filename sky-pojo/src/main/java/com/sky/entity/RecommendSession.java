package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 发起推荐的用户 id。
     */
    private Long userId;

    /**
     * 用户原始查询。
     */
    private String userQuery;

    /**
     * 解析后的意图 JSON 字符串，方便后续排查和分析。
     */
    private String parsedIntent;

    /**
     * 推荐请求创建时间。
     */
    private LocalDateTime createTime;
}
