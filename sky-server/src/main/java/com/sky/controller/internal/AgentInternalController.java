package com.sky.controller.internal;

import com.sky.dto.RecommendLogDTO;
import com.sky.result.Result;
import com.sky.service.AgentInternalService;
import com.sky.vo.AgentAvailableItemVO;
import com.sky.vo.AgentRecentOrderVO;
import com.sky.vo.AgentShopStatusVO;
import com.sky.vo.AgentUserPreferenceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/agent")
@Slf4j
public class AgentInternalController {

    @Autowired
    private AgentInternalService agentInternalService;

    @GetMapping("/shop/status")
    public Result<AgentShopStatusVO> getShopStatus() {
        return Result.success(agentInternalService.getShopStatus());
    }

    @GetMapping("/dishes/available")
    public Result<List<AgentAvailableItemVO>> listAvailableDishes() {
        return Result.success(agentInternalService.listAvailableDishes());
    }

    @GetMapping("/setmeals/available")
    public Result<List<AgentAvailableItemVO>> listAvailableSetmeals() {
        return Result.success(agentInternalService.listAvailableSetmeals());
    }

    @GetMapping("/users/{userId}/preferences")
    public Result<AgentUserPreferenceVO> getUserPreferences(@PathVariable Long userId) {
        return Result.success(agentInternalService.getUserPreferences(userId));
    }

    @GetMapping("/users/{userId}/orders/recent")
    public Result<List<AgentRecentOrderVO>> listRecentOrders(@PathVariable Long userId,
                                                             @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(agentInternalService.listRecentOrders(userId, days));
    }

    @PostMapping("/recommend/log")
    public Result<Long> saveRecommendLog(@RequestBody RecommendLogDTO recommendLogDTO) {
        Long sessionId = agentInternalService.saveRecommendLog(recommendLogDTO);
        log.info("saved recommend log, sessionId={}", sessionId);
        return Result.success(sessionId);
    }
}
