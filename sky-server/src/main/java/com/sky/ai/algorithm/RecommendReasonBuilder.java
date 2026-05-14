package com.sky.ai.algorithm;

import com.sky.dto.RecommendDishDTO;
import com.sky.dto.RecommendIntentDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecommendReasonBuilder {

    /**
     * 为推荐结果生成可解释理由。
     *
     * 这里的理由来自后端已经计算出的意图和菜品信息，不依赖大模型。
     * 即使千问接口不可用，系统也能返回“可解释”的本地推荐结果。
     */
    public String build(RecommendDishDTO dish, RecommendIntentDTO intent) {
        List<String> reasons = new ArrayList<>();
        if (!intent.getTasteTags().isEmpty()) {
            reasons.add("口味匹配" + intent.getTasteTags());
        }
        if (!intent.getNutritionTags().isEmpty()) {
            reasons.add("符合" + intent.getNutritionTags() + "需求");
        }
        if (intent.getBudget() != null) {
            reasons.add("价格在" + intent.getBudget().stripTrailingZeros().toPlainString() + "元预算内");
        }
        if (dish.getSalesCount() != null && dish.getSalesCount() > 0) {
            reasons.add("近期销量较好");
        }
        if (reasons.isEmpty()) {
            reasons.add("综合价格、销量和上架状态表现较好");
        }
        return String.join("，", reasons);
    }
}
