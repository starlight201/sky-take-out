package com.sky.ai.service.impl;

import com.sky.ai.prompt.RecommendPromptTemplate;
import com.sky.ai.service.QwenAgentClient;
import com.sky.ai.service.RecommendAgentService;
import com.sky.ai.service.RecommendCoreService;
import com.sky.dto.RecommendDishDTO;
import com.sky.dto.RecommendRequest;
import com.sky.entity.AgentCallLog;
import com.sky.mapper.AgentCallLogMapper;
import com.sky.properties.QwenProperties;
import com.sky.vo.RecommendAgentResponseVO;
import com.sky.vo.RecommendResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RecommendAgentServiceImpl implements RecommendAgentService {

    @Autowired
    private RecommendCoreService recommendCoreService;

    @Autowired
    private QwenAgentClient qwenAgentClient;

    @Autowired
    private AgentCallLogMapper agentCallLogMapper;

    @Autowired
    private QwenProperties qwenProperties;

    @Autowired
    private RecommendPromptTemplate recommendPromptTemplate;

    @Override
    public RecommendAgentResponseVO recommend(RecommendRequest request) {
        /*
         * Agent 推荐不是让大模型直接查库或直接决定菜品。
         * 推荐结果仍然由 RecommendCoreService 产出，千问只负责把结果组织成
         * 更像真人点餐助手的回答。这样既能利用大模型表达能力，也能保证结果可控。
         */
        RecommendResponseVO response = recommendCoreService.recommend(request);
        String prompt = recommendPromptTemplate.build(request, response);
        String answer;
        long start = System.currentTimeMillis();
        try {
            // 正常路径：调用千问兼容 OpenAI SDK 的 Chat Completions 接口生成回答。
            answer = qwenAgentClient.complete(prompt);
            saveAgentLog(request, prompt, answer, true, null, System.currentTimeMillis() - start);
        } catch (Exception e) {
            // 降级路径：模型不可用、API Key 未配置或网络异常时，仍返回本地推荐结果。
            answer = buildFallbackAnswer(response);
            saveAgentLog(request, prompt, answer, false, e.getMessage(), System.currentTimeMillis() - start);
        }

        return RecommendAgentResponseVO.builder()
                .answer(answer)
                .recommendItems(response.getItems())
                .build();
    }

    /**
     * 本地降级回答。
     *
     * 这是生产项目里很重要的兜底设计：外部大模型服务不可用时，用户依然能看到
     * 后端算法算出的菜品和理由，而不是接口直接失败。
     */
    private String buildFallbackAnswer(RecommendResponseVO response) {
        List<RecommendDishDTO> items = response.getItems();
        if (items == null || items.isEmpty()) {
            return "我已经按你的需求筛选了一遍，但暂时没有找到完全符合条件的上架菜品。可以放宽预算或减少忌口条件再试一次。";
        }
        StringBuilder answer = new StringBuilder();
        answer.append("我根据你的需求筛选了真实上架菜品，优先考虑了口味、预算、忌口、标签和销量。比较推荐：");
        for (int i = 0; i < items.size(); i++) {
            RecommendDishDTO item = items.get(i);
            answer.append("\n").append(i + 1).append(". ")
                    .append(item.getName()).append("，")
                    .append(item.getPrice()).append("元。理由：")
                    .append(item.getReason()).append("。");
        }
        return answer.toString();
    }

    /**
     * 保存大模型调用日志。
     *
     * 记录 prompt、response、是否成功、错误信息和耗时，方便排查模型调用问题，
     * 也方便面试时说明“可观测性”和“线上问题定位”的设计。
     */
    private void saveAgentLog(RecommendRequest request, String prompt, String response, boolean success, String errorMessage, long latencyMs) {
        try {
            AgentCallLog log = AgentCallLog.builder()
                    .userId(request.getUserId())
                    .modelName(qwenProperties.getModel())
                    .userQuery(request.getQuery())
                    .prompt(prompt)
                    .response(response)
                    .success(success ? 1 : 0)
                    .errorMessage(errorMessage)
                    .latencyMs(latencyMs)
                    .build();
            agentCallLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("save agent call log failed", e);
        }
    }
}
