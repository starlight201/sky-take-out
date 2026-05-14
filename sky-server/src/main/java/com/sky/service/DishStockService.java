package com.sky.service;

import com.sky.dto.DishStockAdjustDTO;
import com.sky.dto.DishStockDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface DishStockService {

    Integer getStock(Long dishId);

    void setStock(DishStockDTO dishStockDTO);

    void increase(DishStockAdjustDTO adjustDTO);

    void decrease(DishStockAdjustDTO adjustDTO);

    void checkDishStock(Long dishId, Integer quantity);

    void checkSetmealStock(Long setmealId, Integer quantity);

    void deductShoppingCartStock(List<ShoppingCart> cartList);
}
