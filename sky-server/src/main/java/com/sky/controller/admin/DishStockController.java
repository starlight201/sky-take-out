package com.sky.controller.admin;

import com.sky.dto.DishStockAdjustDTO;
import com.sky.dto.DishStockDTO;
import com.sky.result.Result;
import com.sky.service.DishStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dish-stock")
@Slf4j
public class DishStockController {

    @Autowired
    private DishStockService dishStockService;

    @GetMapping("/{dishId}")
    public Result<Integer> getStock(@PathVariable Long dishId) {
        return Result.success(dishStockService.getStock(dishId));
    }

    @PutMapping
    public Result<String> setStock(@RequestBody DishStockDTO dishStockDTO) {
        log.info("set dish stock: {}", dishStockDTO);
        dishStockService.setStock(dishStockDTO);
        return Result.success();
    }

    @PostMapping("/increase")
    public Result<String> increase(@RequestBody DishStockAdjustDTO adjustDTO) {
        log.info("increase dish stock: {}", adjustDTO);
        dishStockService.increase(adjustDTO);
        return Result.success();
    }

    @PostMapping("/decrease")
    public Result<String> decrease(@RequestBody DishStockAdjustDTO adjustDTO) {
        log.info("decrease dish stock: {}", adjustDTO);
        dishStockService.decrease(adjustDTO);
        return Result.success();
    }
}
