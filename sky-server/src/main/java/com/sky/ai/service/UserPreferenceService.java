package com.sky.ai.service;

import com.sky.entity.UserFoodPreference;

import java.util.List;

public interface UserPreferenceService {

    /**
     * 新增用户饮食偏好。
     */
    void save(UserFoodPreference preference);

    /**
     * 查询用户饮食偏好。
     */
    List<UserFoodPreference> listByUserId(Long userId);

    /**
     * 删除用户饮食偏好。
     */
    void delete(Long id);
}
