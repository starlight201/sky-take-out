package com.sky.ai.service.impl;

import com.sky.ai.service.UserPreferenceService;
import com.sky.entity.UserFoodPreference;
import com.sky.mapper.UserFoodPreferenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {

    @Autowired
    private UserFoodPreferenceMapper userFoodPreferenceMapper;

    @Override
    public void save(UserFoodPreference preference) {
        // source 标识偏好来源：manual 表示用户手动维护，后续可扩展 order/infer 表示从订单推断。
        if (preference.getSource() == null) {
            preference.setSource("manual");
        }
        // weight 表示偏好强度，默认 1。过敏、强烈忌口可以配置更高权重。
        if (preference.getWeight() == null) {
            preference.setWeight(BigDecimal.ONE);
        }
        userFoodPreferenceMapper.insert(preference);
    }

    @Override
    public List<UserFoodPreference> listByUserId(Long userId) {
        // 推荐算法会按用户 id 读取这些偏好，参与个性化打分。
        return userFoodPreferenceMapper.listByUserId(userId);
    }

    @Override
    public void delete(Long id) {
        // 删除用户不再需要的偏好配置。
        userFoodPreferenceMapper.deleteById(id);
    }
}
