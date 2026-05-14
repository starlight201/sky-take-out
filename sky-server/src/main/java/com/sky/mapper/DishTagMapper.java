package com.sky.mapper;

import com.sky.entity.DishTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishTagMapper {

    /**
     * 新增菜品标签。
     */
    @Insert("insert into dish_tag(dish_id, tag_name, tag_type, weight, create_time) " +
            "values(#{dishId}, #{tagName}, #{tagType}, #{weight}, now())")
    void insert(DishTag dishTag);

    /**
     * 查询单个菜品的标签，权重高的排在前面，方便管理端查看。
     */
    @Select("select * from dish_tag where dish_id = #{dishId} order by tag_type, weight desc, id desc")
    List<DishTag> listByDishId(Long dishId);

    /**
     * 查询所有启售菜品的标签，用于推荐流程批量组装菜品特征。
     */
    @Select("select * from dish_tag where dish_id in (select id from dish where status = 1)")
    List<DishTag> listEnabledDishTags();

    /**
     * 按标签 id 删除。
     */
    @Delete("delete from dish_tag where id = #{id}")
    void deleteById(Long id);
}
