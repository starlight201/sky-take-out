package com.sky.service;

import com.sky.dto.DishTagDTO;
import com.sky.dto.DishTagPageQueryDTO;
import com.sky.dto.RecommendLogPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.RecommendLogDetailVO;

public interface AgentAdminService {

    void saveDishTag(DishTagDTO dishTagDTO);

    void updateDishTag(DishTagDTO dishTagDTO);

    void deleteDishTag(Long id);

    PageResult pageDishTags(DishTagPageQueryDTO queryDTO);

    PageResult pageRecommendLogs(RecommendLogPageQueryDTO queryDTO);

    RecommendLogDetailVO getRecommendLogDetail(Long sessionId);
}
