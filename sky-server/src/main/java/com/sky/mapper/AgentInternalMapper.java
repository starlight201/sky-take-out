package com.sky.mapper;

import com.sky.entity.AgentToolCallLog;
import com.sky.entity.RecommendResult;
import com.sky.entity.RecommendSession;
import com.sky.entity.UserFoodPreference;
import com.sky.vo.AgentAvailableItemRawVO;
import com.sky.vo.AgentRecentOrderRawVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AgentInternalMapper {

    List<AgentAvailableItemRawVO> listAvailableDishes();

    List<AgentAvailableItemRawVO> listAvailableSetmeals();

    List<UserFoodPreference> listUserPreferences(Long userId);

    List<AgentRecentOrderRawVO> listRecentOrders(@Param("userId") Long userId,
                                                 @Param("beginTime") LocalDateTime beginTime);

    void insertRecommendSession(RecommendSession recommendSession);

    void insertRecommendResult(RecommendResult recommendResult);

    void insertAgentToolCallLog(AgentToolCallLog agentToolCallLog);
}
