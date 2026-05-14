package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecommendRequest implements Serializable {

    /**
     * 用户 id。
     * 用户端接口可不传，由登录态自动填充；管理端调试接口可以手动指定。
     */
    private Long userId;

    /**
     * 用户自然语言需求，例如“我想吃辣一点、30元以内、不要香菜”。
     */
    private String query;

    /**
     * 返回推荐数量，不传时服务端默认取 5 个。
     */
    private Integer topN;
}
