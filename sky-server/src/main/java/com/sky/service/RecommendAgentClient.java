package com.sky.service;

import com.sky.vo.RecommendAgentResponseVO;

import java.util.function.Consumer;

public interface RecommendAgentClient {

    RecommendAgentResponseVO recommend(Long userId, String query);

    void streamRecommend(Long userId, String query, Consumer<String> lineConsumer);
}
