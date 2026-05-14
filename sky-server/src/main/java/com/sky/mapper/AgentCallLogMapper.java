package com.sky.mapper;

import com.sky.entity.AgentCallLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AgentCallLogMapper {

    /**
     * 保存一次大模型调用日志。
     */
    @Insert("insert into agent_call_log(user_id, model_name, user_query, prompt, response, success, error_message, latency_ms, create_time) " +
            "values(#{userId}, #{modelName}, #{userQuery}, #{prompt}, #{response}, #{success}, #{errorMessage}, #{latencyMs}, now())")
    void insert(AgentCallLog log);

    /**
     * 查询最近的模型调用日志，用于管理端排查问题。
     */
    @Select("select * from agent_call_log order by create_time desc limit #{limit}")
    List<AgentCallLog> listRecent(@Param("limit") Integer limit);

    /**
     * 查询指定用户最近的模型调用日志。
     */
    @Select("select * from agent_call_log where user_id = #{userId} order by create_time desc limit #{limit}")
    List<AgentCallLog> listRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
}
