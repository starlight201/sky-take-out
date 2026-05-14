package com.sky.ai.prompt;

import com.alibaba.fastjson.JSON;
import com.sky.dto.RecommendRequest;
import com.sky.vo.RecommendResponseVO;
import org.springframework.stereotype.Component;

@Component
public class RecommendPromptTemplate {

    /**
     * 大模型角色设定。
     * 注意：模型只负责“表达”，不负责“决定推荐哪些菜”。
     */
    private static final String SYSTEM_ROLE = "你是苍穹外卖的智能点餐推荐助手。";

    /**
     * Prompt 约束条件。
     * 这些限制用于避免大模型编造菜品、价格或标签，让回答严格基于后端结果。
     */
    private static final String CONSTRAINTS =
            "请严格遵守：\n" +
            "1. 只能基于【后端推荐结果】回答，不能编造不存在的菜品、价格、分数或标签。\n" +
            "2. 如果推荐结果为空，要说明暂时没有完全匹配的上架菜品，并建议用户放宽条件。\n" +
            "3. 如果用户提出预算，不要推荐超出预算的菜品。\n" +
            "4. 如果用户提出忌口，回答中要强调已经避开相关菜品。\n" +
            "5. 回答要自然、简洁，适合外卖点餐场景。";

    private static final String OUTPUT_REQUIREMENT =
            "请输出一段中文推荐说明，包含推荐菜品名、价格和推荐理由。";

    /**
     * 组装完整 Prompt。
     *
     * 面试中可以这样解释：后端先完成可控的业务检索和排序，再把结构化结果
     * 放进 Prompt，让大模型生成更自然的点餐话术。
     */
    public String build(RecommendRequest request, RecommendResponseVO response) {
        return SYSTEM_ROLE + "\n\n"
                + CONSTRAINTS + "\n\n"
                + buildContext(request, response) + "\n\n"
                + OUTPUT_REQUIREMENT;
    }

    /**
     * 将业务上下文转成模型可理解的文本。
     * 推荐列表使用 JSON，是为了保留价格、分数、理由、标签等结构化字段。
     */
    private String buildContext(RecommendRequest request, RecommendResponseVO response) {
        StringBuilder context = new StringBuilder();
        context.append("用户ID：").append(request.getUserId()).append("\n");
        context.append("用户需求：").append(request.getQuery()).append("\n");
        context.append("解析意图：").append(JSON.toJSONString(response.getIntent())).append("\n");
        context.append("后端推荐结果：").append(JSON.toJSONString(response.getItems()));
        return context.toString();
    }
}
