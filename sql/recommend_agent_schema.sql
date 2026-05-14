-- 智能菜品推荐 Agent 扩展表
-- 说明：不新增 dish_stock，不引入实时库存业务。

CREATE TABLE IF NOT EXISTS dish_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名，如 辣、低脂、高蛋白、含香菜',
    tag_type VARCHAR(50) COMMENT '标签类型，如 taste、nutrition、ingredient、scene',
    weight DECIMAL(5,2) DEFAULT 1.00 COMMENT '标签权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dish_tag (dish_id, tag_name),
    KEY idx_dish_tag_dish_id (dish_id),
    KEY idx_dish_tag_name (tag_name)
) COMMENT '菜品标签表';

CREATE TABLE IF NOT EXISTS user_food_preference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    preference_type VARCHAR(50) NOT NULL COMMENT 'like/dislike/allergy/budget/taste',
    preference_value VARCHAR(100) NOT NULL COMMENT '偏好值，如 辣、不吃香菜、低脂、30',
    source VARCHAR(50) COMMENT 'manual/history/agent',
    weight DECIMAL(5,2) DEFAULT 1.00,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_food_preference_user_id (user_id),
    KEY idx_user_food_preference_type (preference_type)
) COMMENT '用户饮食偏好表';

CREATE TABLE IF NOT EXISTS dish_sales_stat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    sales_count INT DEFAULT 0 COMMENT '销量',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type VARCHAR(20) DEFAULT 'day' COMMENT 'day/week/month',
    UNIQUE KEY uk_dish_sales_stat (dish_id, stat_date, stat_type),
    KEY idx_dish_sales_stat_dish_id (dish_id),
    KEY idx_dish_sales_stat_date (stat_date)
) COMMENT '菜品销量统计表';

CREATE TABLE IF NOT EXISTS recommend_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_query TEXT NOT NULL COMMENT '用户原始问题',
    parsed_intent JSON COMMENT '解析出的用户意图',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_recommend_session_user_id (user_id),
    KEY idx_recommend_session_create_time (create_time)
) COMMENT '推荐会话表';

CREATE TABLE IF NOT EXISTS recommend_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT '推荐会话ID',
    dish_id BIGINT COMMENT '菜品ID',
    setmeal_id BIGINT COMMENT '套餐ID',
    score DECIMAL(8,2) COMMENT '推荐分数',
    reason VARCHAR(500) COMMENT '推荐理由',
    rank_no INT COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_recommend_result_session_id (session_id),
    KEY idx_recommend_result_dish_id (dish_id),
    KEY idx_recommend_result_setmeal_id (setmeal_id)
) COMMENT '推荐结果表';

CREATE TABLE IF NOT EXISTS agent_tool_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT COMMENT '推荐会话ID',
    tool_name VARCHAR(100) NOT NULL COMMENT '工具名称',
    request_params TEXT COMMENT '请求参数摘要',
    response_summary TEXT COMMENT '响应摘要',
    success TINYINT DEFAULT 1 COMMENT '是否成功',
    cost_ms INT COMMENT '耗时毫秒',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_agent_tool_call_log_session_id (session_id),
    KEY idx_agent_tool_call_log_tool_name (tool_name)
) COMMENT 'Agent 工具调用日志表';
