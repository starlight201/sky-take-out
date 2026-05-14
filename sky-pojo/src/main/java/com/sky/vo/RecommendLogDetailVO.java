package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RecommendLogDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private RecommendSessionLogVO session;
    private List<RecommendResultLogVO> results;
    private List<AgentToolCallLogVO> toolCalls;
}
