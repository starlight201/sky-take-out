package com.sky.controller.admin;

import com.sky.ai.service.DishFeatureService;
import com.sky.entity.DishTag;
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
@RequestMapping("/admin/dish")
public class DishFeatureController {

    @Autowired
    private DishFeatureService dishFeatureService;

    @PostMapping("/tag")
    public Result<String> saveTag(@RequestBody DishTag dishTag) {
        // 管理端维护菜品推荐标签，例如“微辣”“下饭”“高蛋白”。
        dishFeatureService.saveTag(dishTag);
        return Result.success();
    }

    @GetMapping("/{dishId}/tags")
    public Result<List<DishTag>> listTags(@PathVariable Long dishId) {
        // 查看某个菜品的标签，方便运营人员检查推荐特征是否维护正确。
        return Result.success(dishFeatureService.listTags(dishId));
    }

    @DeleteMapping("/tag/{id}")
    public Result<String> deleteTag(@PathVariable Long id) {
        // 删除不准确或过期的菜品标签。
        dishFeatureService.deleteTag(id);
        return Result.success();
    }

}
