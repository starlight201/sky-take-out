package com.sky.ai.service.impl;

import com.sky.ai.service.DishFeatureService;
import com.sky.entity.DishTag;
import com.sky.mapper.DishTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DishFeatureServiceImpl implements DishFeatureService {

    @Autowired
    private DishTagMapper dishTagMapper;

    @Override
    public void saveTag(DishTag dishTag) {
        // 标签权重用于后续扩展更细的打分策略；未传时默认 1，表示普通权重。
        if (dishTag.getWeight() == null) {
            dishTag.setWeight(BigDecimal.ONE);
        }
        dishTagMapper.insert(dishTag);
    }

    @Override
    public List<DishTag> listTags(Long dishId) {
        // 管理端查看某个菜品已维护的全部推荐标签。
        return dishTagMapper.listByDishId(dishId);
    }

    @Override
    public void deleteTag(Long id) {
        // 当前采用物理删除，适合二开演示；生产可扩展为 status 逻辑删除。
        dishTagMapper.deleteById(id);
    }

}
