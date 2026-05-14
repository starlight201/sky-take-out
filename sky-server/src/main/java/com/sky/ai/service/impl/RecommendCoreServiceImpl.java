package com.sky.ai.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.ai.algorithm.RecommendFilter;
import com.sky.ai.algorithm.RecommendIntentParser;
import com.sky.ai.algorithm.RecommendReasonBuilder;
import com.sky.ai.algorithm.RecommendScorer;
import com.sky.ai.service.RecommendCoreService;
import com.sky.dto.RecommendDishDTO;
import com.sky.dto.RecommendIntentDTO;
import com.sky.dto.RecommendRequest;
import com.sky.entity.DishTag;
import com.sky.entity.RecommendResult;
import com.sky.entity.RecommendSession;
import com.sky.entity.UserFoodPreference;
import com.sky.mapper.DishTagMapper;
import com.sky.mapper.RecommendMapper;
import com.sky.mapper.UserFoodPreferenceMapper;
import com.sky.vo.RecommendResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendCoreServiceImpl implements RecommendCoreService {

    @Autowired
    private RecommendIntentParser intentParser;

    @Autowired
    private RecommendFilter recommendFilter;

    @Autowired
    private RecommendScorer recommendScorer;

    @Autowired
    private RecommendReasonBuilder reasonBuilder;

    @Autowired
    private RecommendMapper recommendMapper;

    @Autowired
    private DishTagMapper dishTagMapper;

    @Autowired
    private UserFoodPreferenceMapper userFoodPreferenceMapper;

    @Override
    @Transactional
    public RecommendResponseVO recommend(RecommendRequest request) {
        /*
         * 核心推荐流程：
         * 1. 解析用户自然语言，得到预算、口味、营养、场景、忌口等结构化意图；
         * 2. 查询用户长期偏好，用于个性化加减分；
         * 3. 查询上架菜品、销量等候选数据；
         * 4. 补充菜品标签，形成完整可打分特征；
         * 5. 先按硬约束过滤，再按综合评分排序；
         * 6. 保存推荐会话和推荐明细，方便后续运营分析和面试演示。
         */
        RecommendIntentDTO intent = intentParser.parse(request.getQuery());
        List<UserFoodPreference> preferences = userFoodPreferenceMapper.listByUserId(request.getUserId());
        List<RecommendDishDTO> candidates = recommendMapper.listCandidates();
        fillTags(candidates);

        List<RecommendDishDTO> filtered = recommendFilter.filter(candidates, intent);
        int maxSales = maxSales(filtered);
        for (RecommendDishDTO dish : filtered) {
            BigDecimal score = recommendScorer.score(dish, intent, preferences, maxSales);
            dish.setScore(score);
            dish.setReason(reasonBuilder.build(dish, intent));
        }
        Collections.sort(filtered, new Comparator<RecommendDishDTO>() {
            @Override
            public int compare(RecommendDishDTO o1, RecommendDishDTO o2) {
                return o2.getScore().compareTo(o1.getScore());
            }
        });

        int topN = request.getTopN() == null || request.getTopN() <= 0 ? 5 : request.getTopN();
        List<RecommendDishDTO> items = filtered.size() > topN ? new ArrayList<>(filtered.subList(0, topN)) : filtered;
        saveRecommendLog(request, intent, items);

        return RecommendResponseVO.builder()
                .query(request.getQuery())
                .intent(intent)
                .items(items)
                .build();
    }

    /**
     * 给候选菜品填充标签。
     *
     * dish 表本身不适合存多个标签，所以二开时新增 dish_tag 表。
     * 这里一次性查出所有启售菜品标签，再按 dishId 组装，避免循环查库。
     */
    private void fillTags(List<RecommendDishDTO> candidates) {
        List<DishTag> tags = dishTagMapper.listEnabledDishTags();
        Map<Long, List<String>> tagMap = new HashMap<>();
        for (DishTag tag : tags) {
            List<String> values = tagMap.get(tag.getDishId());
            if (values == null) {
                values = new ArrayList<>();
                tagMap.put(tag.getDishId(), values);
            }
            values.add(tag.getTagName());
        }
        for (RecommendDishDTO dish : candidates) {
            List<String> values = tagMap.get(dish.getDishId());
            dish.setTags(values == null ? new ArrayList<String>() : values);
        }
    }

    /**
     * 计算候选菜品中的最大销量。
     * 后续会把每道菜销量换算成 0-100 分，避免绝对销量过大影响总分。
     */
    private int maxSales(List<RecommendDishDTO> dishes) {
        int max = 0;
        for (RecommendDishDTO dish : dishes) {
            if (dish.getSalesCount() != null && dish.getSalesCount() > max) {
                max = dish.getSalesCount();
            }
        }
        return max;
    }

    /**
     * 保存推荐链路日志。
     *
     * recommend_session 记录一次用户请求和解析后的意图；
     * recommend_result 记录该次请求最终推荐了哪些菜、排名和理由。
     * 这部分数据后续可以用于效果分析、A/B 测试或面试时展示闭环思路。
     */
    private void saveRecommendLog(RecommendRequest request, RecommendIntentDTO intent, List<RecommendDishDTO> items) {
        RecommendSession session = RecommendSession.builder()
                .userId(request.getUserId())
                .userQuery(request.getQuery())
                .parsedIntent(JSON.toJSONString(intent))
                .build();
        recommendMapper.insertSession(session);

        for (int i = 0; i < items.size(); i++) {
            RecommendDishDTO item = items.get(i);
            RecommendResult result = RecommendResult.builder()
                    .sessionId(session.getId())
                    .dishId(item.getDishId())
                    .score(item.getScore())
                    .reason(item.getReason())
                    .rankNo(i + 1)
                    .build();
            recommendMapper.insertResult(result);
        }
    }
}
