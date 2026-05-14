package com.sky.ai.service;

import com.sky.entity.DishTag;

import java.util.List;

public interface DishFeatureService {

    /**
     * 新增菜品标签。
     */
    void saveTag(DishTag dishTag);

    /**
     * 查询指定菜品的全部标签。
     */
    List<DishTag> listTags(Long dishId);

    /**
     * 删除菜品标签。
     */
    void deleteTag(Long id);

}
