package com.sky.mapper;

import com.sky.entity.DishStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishStockMapper {

    @Select("select * from dish_stock where dish_id = #{dishId}")
    DishStock getByDishId(Long dishId);

    void insert(DishStock dishStock);

    void upsert(DishStock dishStock);

    int increase(@Param("dishId") Long dishId, @Param("quantity") Integer quantity);

    int decreaseWithVersion(@Param("id") Long id,
                            @Param("version") Integer version,
                            @Param("quantity") Integer quantity);

    void deleteByDishIds(List<Long> dishIds);
}
