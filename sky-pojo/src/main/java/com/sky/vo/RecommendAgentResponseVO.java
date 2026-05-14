package com.sky.vo;

import com.sky.dto.RecommendDishDTO;
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
public class RecommendAgentResponseVO implements Serializable {

    /**
     * 大模型生成的自然语言回答；模型不可用时返回本地降级话术。
     */
    private String answer;

    /**
     * 后端真实推荐出来的菜品明细。
     */
    private List<RecommendDishDTO> recommendItems;
}
