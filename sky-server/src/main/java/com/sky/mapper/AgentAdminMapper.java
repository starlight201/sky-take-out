package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishTagPageQueryDTO;
import com.sky.dto.RecommendLogPageQueryDTO;
import com.sky.entity.DishTag;
import com.sky.vo.AgentToolCallLogVO;
import com.sky.vo.RecommendResultLogVO;
import com.sky.vo.RecommendSessionLogVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AgentAdminMapper {

    void insertDishTag(DishTag dishTag);

    void updateDishTag(DishTag dishTag);

    @Delete("delete from dish_tag where id = #{id}")
    void deleteDishTag(Long id);

    @Select("select * from dish_tag where id = #{id}")
    DishTag getDishTagById(Long id);

    Page<DishTag> pageDishTags(DishTagPageQueryDTO queryDTO);

    Page<RecommendSessionLogVO> pageRecommendSessions(RecommendLogPageQueryDTO queryDTO);

    RecommendSessionLogVO getRecommendSession(Long sessionId);

    List<RecommendResultLogVO> listRecommendResults(Long sessionId);

    List<AgentToolCallLogVO> listToolCalls(Long sessionId);
}
