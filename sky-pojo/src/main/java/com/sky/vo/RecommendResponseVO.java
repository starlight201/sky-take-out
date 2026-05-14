package com.sky.vo;

import com.sky.dto.RecommendDishDTO;
import com.sky.dto.RecommendIntentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendResponseVO implements Serializable {

    /**
     * 用户原始查询。
     */
    private String query;

    /**
     * 后端解析出的结构化意图。
     */
    private RecommendIntentDTO intent;

    /**
     * 推荐菜品列表，已经按 score 从高到低排序。
     */
    private List<RecommendDishDTO> items;
}
