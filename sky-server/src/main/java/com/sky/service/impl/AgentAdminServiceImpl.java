package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishTagDTO;
import com.sky.dto.DishTagPageQueryDTO;
import com.sky.dto.RecommendLogPageQueryDTO;
import com.sky.entity.DishTag;
import com.sky.mapper.AgentAdminMapper;
import com.sky.result.PageResult;
import com.sky.service.AgentAdminService;
import com.sky.vo.RecommendLogDetailVO;
import com.sky.vo.RecommendSessionLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AgentAdminServiceImpl implements AgentAdminService {

    @Autowired
    private AgentAdminMapper agentAdminMapper;

    public void saveDishTag(DishTagDTO dishTagDTO) {
        DishTag dishTag = new DishTag();
        BeanUtils.copyProperties(dishTagDTO, dishTag);
        if (dishTag.getWeight() == null) {
            dishTag.setWeight(BigDecimal.ONE);
        }
        dishTag.setCreateTime(LocalDateTime.now());
        agentAdminMapper.insertDishTag(dishTag);
    }

    public void updateDishTag(DishTagDTO dishTagDTO) {
        DishTag dishTag = new DishTag();
        BeanUtils.copyProperties(dishTagDTO, dishTag);
        agentAdminMapper.updateDishTag(dishTag);
    }

    public void deleteDishTag(Long id) {
        agentAdminMapper.deleteDishTag(id);
    }

    public PageResult pageDishTags(DishTagPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<DishTag> page = agentAdminMapper.pageDishTags(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public PageResult pageRecommendLogs(RecommendLogPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<RecommendSessionLogVO> page = agentAdminMapper.pageRecommendSessions(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public RecommendLogDetailVO getRecommendLogDetail(Long sessionId) {
        RecommendLogDetailVO detailVO = new RecommendLogDetailVO();
        detailVO.setSession(agentAdminMapper.getRecommendSession(sessionId));
        detailVO.setResults(agentAdminMapper.listRecommendResults(sessionId));
        detailVO.setToolCalls(agentAdminMapper.listToolCalls(sessionId));
        return detailVO;
    }
}
