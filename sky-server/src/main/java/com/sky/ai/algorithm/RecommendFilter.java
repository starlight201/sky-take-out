package com.sky.ai.algorithm;

import com.sky.dto.RecommendDishDTO;
import com.sky.dto.RecommendIntentDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecommendFilter {

    /**
     * 对候选菜品做第一轮硬性过滤。
     *
     * 这里处理的是“必须满足”的条件，例如菜品必须上架、价格不能超过预算、
     * 不能命中用户明确提出的忌口。通过先过滤再打分，可以避免后续算法把
     * 不应该出现的菜品推荐给用户。
     */
    public List<RecommendDishDTO> filter(List<RecommendDishDTO> candidates, RecommendIntentDTO intent) {
        List<RecommendDishDTO> filtered = new ArrayList<>();
        for (RecommendDishDTO candidate : candidates) {
            // status = 1 表示菜品已启售，未启售菜品不能进入推荐列表。
            if (candidate.getStatus() == null || candidate.getStatus() != 1) {
                continue;
            }
            // 用户给出预算时，超预算菜品直接过滤，而不是只降低分数。
            if (intent.getBudget() != null && candidate.getPrice() != null
                    && candidate.getPrice().compareTo(intent.getBudget()) > 0) {
                continue;
            }
            // 忌口属于强约束，例如“不吃牛肉”“花生过敏”，命中后必须剔除。
            if (hitsExcludeTag(candidate, intent)) {
                continue;
            }
            filtered.add(candidate);
        }
        return filtered;
    }

    /**
     * 判断菜品是否命中忌口条件。
     *
     * 匹配范围包含标签、菜品名称和描述。这里使用 contains 双向判断，
     * 是为了兼容“辣”和“微辣”“含牛肉”和“牛肉”这类长短词表达。
     */
    private boolean hitsExcludeTag(RecommendDishDTO dish, RecommendIntentDTO intent) {
        for (String excludeTag : intent.getExcludeTags()) {
            for (String tag : dish.getTags()) {
                if (tag.contains(excludeTag) || excludeTag.contains(tag)) {
                    return true;
                }
            }
            if (dish.getName() != null && dish.getName().contains(excludeTag)) {
                return true;
            }
            if (dish.getDescription() != null && dish.getDescription().contains(excludeTag)) {
                return true;
            }
        }
        return false;
    }
}
