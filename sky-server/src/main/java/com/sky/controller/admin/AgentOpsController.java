package com.sky.controller.admin;

import com.sky.ai.service.QwenAgentClient;
import com.sky.ai.service.RecommendAgentService;
import com.sky.ai.service.RecommendCoreService;
import com.sky.dto.RecommendRequest;
import com.sky.entity.AgentCallLog;
import com.sky.mapper.AgentCallLogMapper;
import com.sky.properties.QwenProperties;
import com.sky.result.Result;
import com.sky.vo.RecommendAgentResponseVO;
import com.sky.vo.RecommendResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/agent")
public class AgentOpsController {

    @Autowired
    private QwenProperties qwenProperties;

    @Autowired
    private QwenAgentClient qwenAgentClient;

    @Autowired
    private RecommendCoreService recommendCoreService;

    @Autowired
    private RecommendAgentService recommendAgentService;

    @Autowired
    private AgentCallLogMapper agentCallLogMapper;

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        /*
         * 管理端查看 Agent 配置状态。
         * 这里只返回 apiKeyConfigured，不返回真实 API Key，避免密钥泄露。
         */
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", qwenProperties.isEnabled());
        data.put("baseUrl", qwenProperties.getBaseUrl());
        data.put("model", qwenProperties.getModel());
        data.put("apiKeyConfigured", qwenProperties.getApiKey() != null && qwenProperties.getApiKey().trim().length() > 0);
        return Result.success(data);
    }

    @GetMapping("/qwen/ping")
    public Result<String> ping(@RequestParam(defaultValue = "你是谁") String message) {
        // 用于联调千问接口是否可用，生产环境可以加权限或只在测试环境开放。
        return Result.success(qwenAgentClient.complete(message));
    }

    @PostMapping("/recommend/raw")
    public Result<RecommendResponseVO> recommendRaw(@RequestBody RecommendRequest request) {
        // 返回后端算法的原始推荐结果，便于调试意图解析、打分和排序是否合理。
        return Result.success(recommendCoreService.recommend(request));
    }

    @PostMapping("/recommend")
    public Result<RecommendAgentResponseVO> recommend(@RequestBody RecommendRequest request) {
        // 返回 Agent 包装后的推荐结果：包含大模型生成回答和后端推荐明细。
        return Result.success(recommendAgentService.recommend(request));
    }

    @GetMapping("/logs")
    public Result<List<AgentCallLog>> logs(@RequestParam(required = false) Long userId,
                                           @RequestParam(defaultValue = "20") Integer limit) {
        // limit 做边界保护，避免一次查询过多日志影响管理端响应速度。
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        if (userId == null) {
            return Result.success(agentCallLogMapper.listRecent(safeLimit));
        }
        return Result.success(agentCallLogMapper.listRecentByUserId(userId, safeLimit));
    }
}
