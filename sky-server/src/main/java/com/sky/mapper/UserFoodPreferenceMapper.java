package com.sky.mapper;

import com.sky.entity.UserFoodPreference;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFoodPreferenceMapper {

    /**
     * 新增用户饮食偏好。
     */
    @Insert("insert into user_food_preference(user_id, preference_type, preference_value, source, weight, create_time, update_time) " +
            "values(#{userId}, #{preferenceType}, #{preferenceValue}, #{source}, #{weight}, now(), now())")
    void insert(UserFoodPreference preference);

    /**
     * 按用户查询偏好，权重高、更新时间新的优先。
     */
    @Select("select * from user_food_preference where user_id = #{userId} order by weight desc, update_time desc")
    List<UserFoodPreference> listByUserId(Long userId);

    /**
     * 删除偏好项。
     */
    @Delete("delete from user_food_preference where id = #{id}")
    void deleteById(Long id);
}
