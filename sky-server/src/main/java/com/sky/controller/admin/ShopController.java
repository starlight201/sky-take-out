package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {
    public  static final String KEY="SHOP_STATUS";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置店铺状态 1: 营业 0: 休息
     * 
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    public Result<String> setShopStatus(@PathVariable Integer status) {
        log.info("设置店铺状态: {}", status == 1 ? "营业" : "休息");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    /**
     * 获取店铺状态 1: 营业 0: 休息
     * 
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        Integer status = (Integer) Optional.ofNullable(redisTemplate.opsForValue().get(KEY)).orElse(0);
        log.info("获取店铺状态: {}", status == 1 ? "营业" : "休息");
        return Result.success(status);
    }

}
