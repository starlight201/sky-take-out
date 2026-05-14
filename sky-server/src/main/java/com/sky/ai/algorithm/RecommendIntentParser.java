package com.sky.ai.algorithm;

import com.sky.dto.RecommendIntentDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecommendIntentParser {

    /**
     * 从用户自然语言中提取预算数字，例如“30元以内”“20块以下”。
     * 当前阶段采用规则解析，优点是稳定、可解释，适合面试中说明业务落地。
     */
    private static final Pattern BUDGET_PATTERN = Pattern.compile("(\\d+)\\s*(元|块|以内|以下)");

    /**
     * 将用户输入解析成结构化意图。
     *
     * 大模型不直接决定推荐结果，而是由后端先把用户需求转换成标签、预算、
     * 场景、忌口等可计算字段，再交给推荐算法处理。这样可以避免“凭空编菜”。
     */
    public RecommendIntentDTO parse(String query) {
        RecommendIntentDTO intent = new RecommendIntentDTO();
        if (query == null) {
            return intent;
        }

        parseTaste(query, intent);
        parseNutrition(query, intent);
        parseExclude(query, intent);
        parseScene(query, intent);
        parseBudget(query, intent);
        return intent;
    }

    /**
     * 解析口味偏好和口味忌口。
     * “不要辣”优先级高于“辣”，所以先判断否定表达。
     */
    private void parseTaste(String query, RecommendIntentDTO intent) {
        if (containsAny(query, "不要辣", "不吃辣", "不能吃辣")) {
            intent.getExcludeTags().add("辣");
            intent.getExcludeTags().add("微辣");
            intent.getExcludeTags().add("重辣");
            return;
        }
        if (query.contains("重辣")) {
            intent.getTasteTags().add("重辣");
        } else if (query.contains("微辣")) {
            intent.getTasteTags().add("微辣");
        } else if (query.contains("辣")) {
            intent.getTasteTags().add("辣");
        }
        if (containsAny(query, "清淡", "少油")) {
            intent.getTasteTags().add("清淡");
        }
        if (containsAny(query, "酸甜", "甜酸")) {
            intent.getTasteTags().add("酸甜");
        }
    }

    /**
     * 解析营养目标，例如健身、减脂。
     * 这里会同时补充营养标签和场景标签，方便后续打分时多维度匹配。
     */
    private void parseNutrition(String query, RecommendIntentDTO intent) {
        if (containsAny(query, "健身", "增肌", "高蛋白")) {
            intent.getNutritionTags().add("高蛋白");
            intent.getNutritionTags().add("低脂");
            intent.getSceneTags().add("适合健身");
            intent.setScene("健身餐");
        }
        if (containsAny(query, "减脂", "减肥", "低脂")) {
            intent.getNutritionTags().add("低脂");
            intent.getNutritionTags().add("少油");
            intent.setScene("减脂餐");
        }
    }

    /**
     * 解析明确忌口或过敏信息。
     * 过敏、忌口属于安全约束，后续过滤阶段会直接剔除命中的菜品。
     */
    private void parseExclude(String query, RecommendIntentDTO intent) {
        if (containsAny(query, "不吃香菜", "不要香菜", "忌香菜")) {
            intent.getExcludeTags().add("香菜");
            intent.getExcludeTags().add("含香菜");
        }
        if (containsAny(query, "不吃牛肉", "不要牛肉")) {
            intent.getExcludeTags().add("牛肉");
            intent.getExcludeTags().add("含牛肉");
        }
        if (containsAny(query, "花生过敏", "不吃花生", "不要花生")) {
            intent.getExcludeTags().add("花生");
            intent.getExcludeTags().add("含花生");
        }
    }

    /**
     * 解析用餐场景，例如午餐、晚餐、下饭、家常。
     * 场景标签不一定是强约束，但会在打分时提升更贴合场景的菜品。
     */
    private void parseScene(String query, RecommendIntentDTO intent) {
        if (query.contains("午餐") || query.contains("中午")) {
            intent.setMealType("午餐");
            intent.getSceneTags().add("适合午餐");
        }
        if (query.contains("晚餐") || query.contains("晚上")) {
            intent.setMealType("晚餐");
            intent.getSceneTags().add("适合晚餐");
        }
        if (containsAny(query, "下饭", "米饭")) {
            intent.getSceneTags().add("下饭");
        }
        if (containsAny(query, "家常", "日常")) {
            intent.getSceneTags().add("家常");
        }
    }

    /**
     * 解析预算。预算会在过滤阶段作为硬限制，确保不会推荐超预算菜品。
     */
    private void parseBudget(String query, RecommendIntentDTO intent) {
        Matcher matcher = BUDGET_PATTERN.matcher(query);
        if (matcher.find()) {
            intent.setBudget(new BigDecimal(matcher.group(1)));
        }
    }

    /**
     * 简单关键词命中工具方法。
     * 当前二开阶段先用规则保证稳定性，后续可以替换为词典、分词或大模型意图识别。
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
