-- 智能菜品推荐 Agent 二开表结构
-- 执行库：sky_take_out

CREATE TABLE IF NOT EXISTS dish_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名',
    tag_type VARCHAR(50) COMMENT '标签类型：taste/nutrition/ingredient/scene',
    weight DECIMAL(5,2) DEFAULT 1.00 COMMENT '标签权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dish_tag_dish_id (dish_id),
    INDEX idx_dish_tag_name (tag_name)
) COMMENT='菜品推荐标签表';

CREATE TABLE IF NOT EXISTS user_food_preference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    preference_type VARCHAR(50) NOT NULL COMMENT 'like/dislike/allergy/budget/taste',
    preference_value VARCHAR(100) NOT NULL COMMENT '偏好值',
    source VARCHAR(50) COMMENT 'manual/history/agent',
    weight DECIMAL(5,2) DEFAULT 1.00,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_food_preference_user_id (user_id)
) COMMENT='用户饮食偏好表';

CREATE TABLE IF NOT EXISTS dish_sales_stat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    sales_count INT DEFAULT 0 COMMENT '销量',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type VARCHAR(20) DEFAULT 'day' COMMENT 'day/week/month',
    INDEX idx_dish_sales_stat_dish_id (dish_id),
    INDEX idx_dish_sales_stat_date (stat_date)
) COMMENT='菜品销量统计表';

CREATE TABLE IF NOT EXISTS recommend_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID',
    user_query TEXT NOT NULL COMMENT '用户原始问题',
    parsed_intent JSON COMMENT '解析出的用户意图',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recommend_session_user_id (user_id)
) COMMENT='推荐会话表';

CREATE TABLE IF NOT EXISTS recommend_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT '推荐会话ID',
    dish_id BIGINT COMMENT '菜品ID',
    setmeal_id BIGINT COMMENT '套餐ID',
    score DECIMAL(8,2) COMMENT '推荐分数',
    reason VARCHAR(500) COMMENT '推荐理由',
    rank_no INT COMMENT '排序名次',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recommend_result_session_id (session_id)
) COMMENT='推荐结果表';

CREATE TABLE IF NOT EXISTS agent_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID',
    model_name VARCHAR(100) COMMENT '模型名称',
    user_query TEXT COMMENT '用户原始问题',
    prompt MEDIUMTEXT COMMENT '发送给模型的提示词',
    response MEDIUMTEXT COMMENT '模型回答或兜底回答',
    success TINYINT DEFAULT 1 COMMENT '1成功 0失败',
    error_message VARCHAR(1000) COMMENT '失败原因',
    latency_ms BIGINT COMMENT '调用耗时毫秒',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_agent_call_log_user_id (user_id),
    INDEX idx_agent_call_log_create_time (create_time)
) COMMENT='Agent模型调用日志表';

-- 示例标签，可按真实菜品 ID 调整
-- insert into dish_tag(dish_id, tag_name, tag_type, weight) values (1, '辣', 'taste', 1.00);
