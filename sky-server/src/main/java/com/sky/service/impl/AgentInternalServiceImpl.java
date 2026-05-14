package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.dto.RecommendLogDTO;
import com.sky.dto.RecommendLogItemDTO;
import com.sky.dto.RecommendToolCallDTO;
import com.sky.entity.AgentToolCallLog;
import com.sky.entity.RecommendResult;
import com.sky.entity.RecommendSession;
import com.sky.entity.UserFoodPreference;
import com.sky.mapper.AgentInternalMapper;
import com.sky.service.AgentInternalService;
import com.sky.vo.AgentAvailableItemRawVO;
import com.sky.vo.AgentAvailableItemVO;
import com.sky.vo.AgentRecentOrderRawVO;
import com.sky.vo.AgentRecentOrderVO;
import com.sky.vo.AgentShopStatusVO;
import com.sky.vo.AgentUserPreferenceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgentInternalServiceImpl implements AgentInternalService {

    private static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AgentInternalMapper agentInternalMapper;

    public AgentShopStatusVO getShopStatus() {
        Integer status = (Integer) Optional.ofNullable(redisTemplate.opsForValue().get(SHOP_STATUS_KEY)).orElse(0);
        return new AgentShopStatusVO(status, status == 1 ? "店铺营业中" : "店铺已打烊");
    }

    public List<AgentAvailableItemVO> listAvailableDishes() {
        return convertItems(agentInternalMapper.listAvailableDishes());
    }

    public List<AgentAvailableItemVO> listAvailableSetmeals() {
        return convertItems(agentInternalMapper.listAvailableSetmeals());
    }

    public AgentUserPreferenceVO getUserPreferences(Long userId) {
        AgentUserPreferenceVO vo = new AgentUserPreferenceVO();
        vo.setUserId(userId);
        List<UserFoodPreference> preferences = agentInternalMapper.listUserPreferences(userId);
        for (UserFoodPreference preference : preferences) {
            String type = preference.getPreferenceType();
            String value = preference.getPreferenceValue();
            if (!StringUtils.hasText(value)) {
                continue;
            }
            if ("like".equalsIgnoreCase(type) || "taste".equalsIgnoreCase(type)) {
                vo.getLikes().add(value);
            } else if ("dislike".equalsIgnoreCase(type)) {
                vo.getDislikes().add(value);
            } else if ("allergy".equalsIgnoreCase(type)) {
                vo.getAllergies().add(value);
            } else if ("budget".equalsIgnoreCase(type)) {
                try {
                    vo.setDefaultBudget(new BigDecimal(value));
                } catch (NumberFormatException ignored) {
                    // Bad preference values should not block recommendations.
                }
            }
        }
        return vo;
    }

    public List<AgentRecentOrderVO> listRecentOrders(Long userId, Integer days) {
        int queryDays = days == null || days <= 0 ? 30 : days;
        LocalDateTime beginTime = LocalDateTime.now().minusDays(queryDays);
        return agentInternalMapper.listRecentOrders(userId, beginTime)
                .stream()
                .map(this::convertRecentOrder)
                .collect(Collectors.toList());
    }

    public Long saveRecommendLog(RecommendLogDTO recommendLogDTO) {
        RecommendSession session = RecommendSession.builder()
                .userId(recommendLogDTO.getUserId())
                .userQuery(recommendLogDTO.getQuery())
                .parsedIntent(JSON.toJSONString(recommendLogDTO.getIntent()))
                .createTime(LocalDateTime.now())
                .build();
        agentInternalMapper.insertRecommendSession(session);

        List<RecommendLogItemDTO> items = recommendLogDTO.getItems();
        if (!CollectionUtils.isEmpty(items)) {
            for (int i = 0; i < items.size(); i++) {
                RecommendLogItemDTO item = items.get(i);
                RecommendResult result = RecommendResult.builder()
                        .sessionId(session.getId())
                        .dishId(item.getDishId())
                        .setmealId(item.getSetmealId())
                        .score(item.getScore())
                        .reason(item.getReason())
                        .rankNo(i + 1)
                        .createTime(LocalDateTime.now())
                        .build();
                agentInternalMapper.insertRecommendResult(result);
            }
        }

        List<RecommendToolCallDTO> toolCalls = recommendLogDTO.getToolCalls();
        if (!CollectionUtils.isEmpty(toolCalls)) {
            for (RecommendToolCallDTO toolCall : toolCalls) {
                AgentToolCallLog log = AgentToolCallLog.builder()
                        .sessionId(session.getId())
                        .toolName(toolCall.getToolName())
                        .requestParams(toolCall.getRequestParams())
                        .responseSummary(toolCall.getResponseSummary())
                        .success(toolCall.getSuccess())
                        .costMs(toolCall.getCostMs())
                        .createTime(LocalDateTime.now())
                        .build();
                agentInternalMapper.insertAgentToolCallLog(log);
            }
        }

        return session.getId();
    }

    private List<AgentAvailableItemVO> convertItems(List<AgentAvailableItemRawVO> rawItems) {
        if (CollectionUtils.isEmpty(rawItems)) {
            return Collections.emptyList();
        }
        List<AgentAvailableItemVO> result = new ArrayList<>();
        for (AgentAvailableItemRawVO raw : rawItems) {
            AgentAvailableItemVO vo = new AgentAvailableItemVO();
            BeanUtils.copyProperties(raw, vo);
            vo.setTags(splitTags(raw.getTagNames()));
            vo.setSalesCount(raw.getSalesCount() == null ? 0 : raw.getSalesCount());
            result.add(vo);
        }
        return result;
    }

    private AgentRecentOrderVO convertRecentOrder(AgentRecentOrderRawVO raw) {
        AgentRecentOrderVO vo = new AgentRecentOrderVO();
        BeanUtils.copyProperties(raw, vo);
        vo.setTags(splitTags(raw.getTagNames()));
        vo.setCount(raw.getCount() == null ? 0 : raw.getCount());
        return vo;
    }

    private List<String> splitTags(String tagNames) {
        if (!StringUtils.hasText(tagNames)) {
            return Collections.emptyList();
        }
        return Arrays.stream(tagNames.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }
}
