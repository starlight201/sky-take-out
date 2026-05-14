package com.sky.ai.service;

import com.sky.dto.RecommendRequest;
import com.sky.vo.RecommendAgentResponseVO;

public interface RecommendAgentService {

    /**
     * 执行 Agent 推荐流程。
     *
     * 在核心推荐结果基础上调用千问生成自然语言回答；模型不可用时返回本地降级回答。
     */
    RecommendAgentResponseVO recommend(RecommendRequest request);
}
