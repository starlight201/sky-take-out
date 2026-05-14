package com.sky.ai.service;

import com.sky.dto.RecommendRequest;
import com.sky.vo.RecommendResponseVO;

public interface RecommendCoreService {

    /**
     * 执行不依赖大模型的核心推荐流程。
     *
     * 返回内容包含用户意图、推荐菜品、分数和后端推荐理由，是整个 Agent 的基础能力。
     */
    RecommendResponseVO recommend(RecommendRequest request);
}
