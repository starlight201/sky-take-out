package com.sky.service;

import com.sky.dto.RecommendLogDTO;
import com.sky.vo.AgentAvailableItemVO;
import com.sky.vo.AgentRecentOrderVO;
import com.sky.vo.AgentShopStatusVO;
import com.sky.vo.AgentUserPreferenceVO;

import java.util.List;

public interface AgentInternalService {

    AgentShopStatusVO getShopStatus();

    List<AgentAvailableItemVO> listAvailableDishes();

    List<AgentAvailableItemVO> listAvailableSetmeals();

    AgentUserPreferenceVO getUserPreferences(Long userId);

    List<AgentRecentOrderVO> listRecentOrders(Long userId, Integer days);

    Long saveRecommendLog(RecommendLogDTO recommendLogDTO);
}
