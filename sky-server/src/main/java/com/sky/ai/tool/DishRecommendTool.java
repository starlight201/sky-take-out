package com.sky.ai.tool;

import com.sky.ai.service.RecommendCoreService;
import com.sky.dto.RecommendRequest;
import com.sky.vo.RecommendResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DishRecommendTool {

    @Autowired
    private RecommendCoreService recommendCoreService;

    /**
     * 推荐工具封装。
     *
     * 目前由后端服务直接调用；如果后续接入真正的 Agent Tool Calling，
     * 这个类可以作为“菜品推荐工具”的入口，把大模型参数转换为业务请求。
     */
    public RecommendResponseVO recommend(Long userId, String query, Integer topN) {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId);
        request.setQuery(query);
        request.setTopN(topN);
        return recommendCoreService.recommend(request);
    }
}
