package com.sky.controller.admin;

import com.sky.dto.DishTagDTO;
import com.sky.dto.DishTagPageQueryDTO;
import com.sky.dto.RecommendLogPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AgentAdminService;
import com.sky.vo.RecommendLogDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/agent")
@Api(tags = "Agent 推荐后台管理接口")
@Slf4j
public class AgentAdminController {

    @Autowired
    private AgentAdminService agentAdminService;

    @PostMapping("/dish-tags")
    @ApiOperation("新增菜品标签")
    public Result<String> saveDishTag(@RequestBody DishTagDTO dishTagDTO) {
        agentAdminService.saveDishTag(dishTagDTO);
        return Result.success();
    }

    @PutMapping("/dish-tags")
    @ApiOperation("修改菜品标签")
    public Result<String> updateDishTag(@RequestBody DishTagDTO dishTagDTO) {
        agentAdminService.updateDishTag(dishTagDTO);
        return Result.success();
    }

    @DeleteMapping("/dish-tags/{id}")
    @ApiOperation("删除菜品标签")
    public Result<String> deleteDishTag(@PathVariable Long id) {
        agentAdminService.deleteDishTag(id);
        return Result.success();
    }

    @GetMapping("/dish-tags/page")
    @ApiOperation("菜品标签分页查询")
    public Result<PageResult> pageDishTags(DishTagPageQueryDTO queryDTO) {
        return Result.success(agentAdminService.pageDishTags(queryDTO));
    }

    @GetMapping("/recommend-logs/page")
    @ApiOperation("推荐日志分页查询")
    public Result<PageResult> pageRecommendLogs(RecommendLogPageQueryDTO queryDTO) {
        return Result.success(agentAdminService.pageRecommendLogs(queryDTO));
    }

    @GetMapping("/recommend-logs/{sessionId}")
    @ApiOperation("推荐日志详情")
    public Result<RecommendLogDetailVO> getRecommendLogDetail(@PathVariable Long sessionId) {
        return Result.success(agentAdminService.getRecommendLogDetail(sessionId));
    }
}
