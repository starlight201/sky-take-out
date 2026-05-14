package com.sky.service.impl;

import com.sky.dto.DishStockAdjustDTO;
import com.sky.dto.DishStockDTO;
import com.sky.entity.DishStock;
import com.sky.entity.SetmealDish;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishStockMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.service.DishStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DishStockServiceImpl implements DishStockService {

    private static final int MAX_OPTIMISTIC_RETRY = 3;

    @Autowired
    private DishStockMapper dishStockMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    public Integer getStock(Long dishId) {
        DishStock dishStock = dishStockMapper.getByDishId(dishId);
        return dishStock == null ? 0 : dishStock.getStock();
    }

    public void setStock(DishStockDTO dishStockDTO) {
        validateQuantity(dishStockDTO.getStock(), "库存数量不能为负数");
        ensureDishExists(dishStockDTO.getDishId());
        DishStock dishStock = DishStock.builder()
                .dishId(dishStockDTO.getDishId())
                .stock(dishStockDTO.getStock())
                .version(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        dishStockMapper.upsert(dishStock);
    }

    public void increase(DishStockAdjustDTO adjustDTO) {
        validatePositive(adjustDTO.getQuantity());
        ensureDishExists(adjustDTO.getDishId());
        int rows = dishStockMapper.increase(adjustDTO.getDishId(), adjustDTO.getQuantity());
        if (rows == 0) {
            DishStock dishStock = DishStock.builder()
                    .dishId(adjustDTO.getDishId())
                    .stock(adjustDTO.getQuantity())
                    .version(0)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            dishStockMapper.insert(dishStock);
        }
    }

    public void decrease(DishStockAdjustDTO adjustDTO) {
        validatePositive(adjustDTO.getQuantity());
        ensureDishExists(adjustDTO.getDishId());
        decreaseWithOptimisticLock(adjustDTO.getDishId(), adjustDTO.getQuantity());
    }

    public void checkDishStock(Long dishId, Integer quantity) {
        if (dishId == null) {
            return;
        }
        validatePositive(quantity);
        if (getStock(dishId) < quantity) {
            throw new OrderBusinessException("菜品库存不足");
        }
    }

    public void checkSetmealStock(Long setmealId, Integer quantity) {
        if (setmealId == null) {
            return;
        }
        validatePositive(quantity);
        List<SetmealDish> setmealDishes = setmealDishMapper.getDishBySetmealId(setmealId);
        if (CollectionUtils.isEmpty(setmealDishes)) {
            return;
        }
        for (SetmealDish setmealDish : setmealDishes) {
            int required = safeCopies(setmealDish.getCopies()) * quantity;
            checkDishStock(setmealDish.getDishId(), required);
        }
    }

    @Transactional
    public void deductShoppingCartStock(List<ShoppingCart> cartList) {
        Map<Long, Integer> requiredMap = new HashMap<>();
        for (ShoppingCart cart : cartList) {
            int cartNumber = cart.getNumber() == null ? 0 : cart.getNumber();
            if (cart.getDishId() != null) {
                requiredMap.merge(cart.getDishId(), cartNumber, Integer::sum);
            } else if (cart.getSetmealId() != null) {
                List<SetmealDish> setmealDishes = setmealDishMapper.getDishBySetmealId(cart.getSetmealId());
                for (SetmealDish setmealDish : setmealDishes) {
                    int required = safeCopies(setmealDish.getCopies()) * cartNumber;
                    requiredMap.merge(setmealDish.getDishId(), required, Integer::sum);
                }
            }
        }

        for (Map.Entry<Long, Integer> entry : requiredMap.entrySet()) {
            decreaseWithOptimisticLock(entry.getKey(), entry.getValue());
        }
    }

    private void decreaseWithOptimisticLock(Long dishId, Integer quantity) {
        for (int i = 0; i < MAX_OPTIMISTIC_RETRY; i++) {
            DishStock dishStock = dishStockMapper.getByDishId(dishId);
            if (dishStock == null || dishStock.getStock() == null || dishStock.getStock() < quantity) {
                throw new OrderBusinessException("菜品库存不足");
            }
            int rows = dishStockMapper.decreaseWithVersion(dishStock.getId(), dishStock.getVersion(), quantity);
            if (rows > 0) {
                return;
            }
        }
        throw new OrderBusinessException("库存更新冲突，请稍后重试");
    }

    private void ensureDishExists(Long dishId) {
        if (dishId == null || dishMapper.getById(dishId) == null) {
            throw new OrderBusinessException("菜品不存在");
        }
    }

    private void validateQuantity(Integer quantity, String message) {
        if (quantity == null || quantity < 0) {
            throw new OrderBusinessException(message);
        }
    }

    private void validatePositive(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new OrderBusinessException("库存变更数量必须大于0");
        }
    }

    private int safeCopies(Integer copies) {
        return copies == null || copies <= 0 ? 1 : copies;
    }
}
