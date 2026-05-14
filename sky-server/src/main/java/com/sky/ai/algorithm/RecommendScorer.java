package com.sky.ai.algorithm;

import com.sky.dto.RecommendDishDTO;
import com.sky.dto.RecommendIntentDTO;
import com.sky.entity.UserFoodPreference;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class RecommendScorer {

    /**
     * 对单个菜品进行综合评分。
     *
     * 权重设计：
     * 1. 标签匹配 35%：最直接反映用户当前需求；
     * 2. 用户偏好 25%：体现个性化；
     * 3. 销量热度 20%：体现大众选择和近期表现；
     * 4. 价格匹配 10%：预算内且价格接近预算通常更符合点餐预期；
     * 5. 场景匹配 10%：例如午餐、下饭、健身餐。
     */
    public BigDecimal score(RecommendDishDTO dish, RecommendIntentDTO intent, List<UserFoodPreference> preferences, int maxSales) {
        double tagScore = matchTags(dish, intent);
        double preferenceScore = matchPreferences(dish, preferences);
        double salesScore = maxSales <= 0 ? 60 : Math.min(100, dish.getSalesCount() * 100.0 / maxSales);
        double priceScore = priceScore(dish, intent);
        double sceneScore = matchList(dish, intent.getSceneTags()) * 100;

        double total = tagScore * 0.35
                + preferenceScore * 0.25
                + salesScore * 0.20
                + priceScore * 0.10
                + sceneScore * 0.10;
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算用户当前输入中的口味标签、营养标签与菜品特征的匹配程度。
     * 如果用户没有表达明确偏好，给一个中性分，避免无条件拉低菜品。
     */
    private double matchTags(RecommendDishDTO dish, RecommendIntentDTO intent) {
        int wanted = intent.getTasteTags().size() + intent.getNutritionTags().size();
        if (wanted == 0) {
            return 60;
        }
        int hit = countHits(dish, intent.getTasteTags()) + countHits(dish, intent.getNutritionTags());
        return Math.min(100, hit * 100.0 / wanted);
    }

    /**
     * 计算长期用户偏好对菜品分数的影响。
     * 喜欢或口味偏好会加分，讨厌或过敏会扣分；weight 越高影响越大。
     */
    private double matchPreferences(RecommendDishDTO dish, List<UserFoodPreference> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return 60;
        }
        double score = 60;
        for (UserFoodPreference preference : preferences) {
            String type = preference.getPreferenceType();
            String value = preference.getPreferenceValue();
            if (value == null) {
                continue;
            }
            boolean hit = containsDishFeature(dish, value);
            if (hit && ("like".equals(type) || "taste".equals(type))) {
                score += weight(preference) * 15;
            }
            if (hit && ("dislike".equals(type) || "allergy".equals(type))) {
                score -= weight(preference) * 25;
            }
        }
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 计算价格匹配分。
     * 已经超预算的菜品会在过滤阶段被剔除，这里只负责在预算内区分价格贴合度。
     */
    private double priceScore(RecommendDishDTO dish, RecommendIntentDTO intent) {
        if (intent.getBudget() == null || dish.getPrice() == null) {
            return 70;
        }
        if (dish.getPrice().compareTo(intent.getBudget()) > 0) {
            return 0;
        }
        double budget = intent.getBudget().doubleValue();
        if (budget <= 0) {
            return 70;
        }
        double ratio = dish.getPrice().doubleValue() / budget;
        return ratio >= 0.6 ? 100 : 80;
    }

    /**
     * 统计目标标签命中数量。
     */
    private int countHits(RecommendDishDTO dish, List<String> tags) {
        int count = 0;
        for (String tag : tags) {
            if (containsDishFeature(dish, tag)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 场景标签只判断是否至少命中一个。
     */
    private double matchList(RecommendDishDTO dish, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return 0.6;
        }
        return countHits(dish, tags) > 0 ? 1 : 0;
    }

    /**
     * 在菜品标签、名称、描述、分类中查找关键特征。
     * 这样即使某些老数据没有完整标签，也能借助名称/描述参与推荐。
     */
    private boolean containsDishFeature(RecommendDishDTO dish, String keyword) {
        for (String tag : dish.getTags()) {
            if (tag.contains(keyword) || keyword.contains(tag)) {
                return true;
            }
        }
        return (dish.getName() != null && dish.getName().contains(keyword))
                || (dish.getDescription() != null && dish.getDescription().contains(keyword))
                || (dish.getCategoryName() != null && dish.getCategoryName().contains(keyword));
    }

    /**
     * 偏好权重默认按 1 处理，避免旧数据没有 weight 时影响算法运行。
     */
    private double weight(UserFoodPreference preference) {
        if (preference.getWeight() == null) {
            return 1;
        }
        return preference.getWeight().doubleValue();
    }
}
