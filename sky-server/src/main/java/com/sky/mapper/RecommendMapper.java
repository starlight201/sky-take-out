package com.sky.mapper;

import com.sky.dto.RecommendDishDTO;
import com.sky.entity.RecommendResult;
import com.sky.entity.RecommendSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecommendMapper {

    /**
     * 查询推荐候选菜品。
     *
     * 只查菜品基础信息、分类和销量聚合，不在 SQL 中写复杂推荐逻辑。
     * 这样 SQL 负责取数，Java 服务层负责过滤、打分和排序，职责更清楚。
     */
    @Select("select d.id dish_id, d.name, d.category_id, c.name category_name, d.price, d.image, d.description, d.status, " +
            "coalesce(sum(dss.sales_count), 0) sales_count " +
            "from dish d " +
            "left join category c on d.category_id = c.id " +
            "left join dish_sales_stat dss on d.id = dss.dish_id " +
            "where d.status = 1 " +
            "group by d.id, d.name, d.category_id, c.name, d.price, d.image, d.description, d.status")
    List<RecommendDishDTO> listCandidates();

    /**
     * 保存一次推荐请求会话，useGeneratedKeys 用于拿到自增 sessionId。
     */
    @Insert("insert into recommend_session(user_id, user_query, parsed_intent, create_time) " +
            "values(#{userId}, #{userQuery}, #{parsedIntent}, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSession(RecommendSession session);

    /**
     * 保存单个推荐结果，记录排名和推荐理由。
     */
    @Insert("insert into recommend_result(session_id, dish_id, setmeal_id, score, reason, rank_no, create_time) " +
            "values(#{sessionId}, #{dishId}, #{setmealId}, #{score}, #{reason}, #{rankNo}, now())")
    void insertResult(RecommendResult result);
}
