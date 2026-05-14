package com.sky.controller.user;

import com.sky.ai.service.UserPreferenceService;
import com.sky.context.BaseContext;
import com.sky.entity.UserFoodPreference;
import com.sky.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/preference")
public class UserPreferenceController {

    @Autowired
    private UserPreferenceService userPreferenceService;

    @PostMapping
    public Result<String> save(@RequestBody UserFoodPreference preference) {
        // 用户新增个人口味偏好。如果前端未传 userId，则使用当前登录用户。
        if (preference.getUserId() == null) {
            preference.setUserId(BaseContext.getCurrentId());
        }
        userPreferenceService.save(preference);
        return Result.success();
    }

    @GetMapping("/{userId}")
    public Result<List<UserFoodPreference>> list(@PathVariable Long userId) {
        // 查询用户偏好，推荐算法会基于这些偏好做个性化加减分。
        return Result.success(userPreferenceService.listByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // 删除用户不再需要的偏好项。
        userPreferenceService.delete(id);
        return Result.success();
    }
}
