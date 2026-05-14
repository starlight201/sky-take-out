/*
 智能菜品推荐 Agent 模拟数据

 使用前提：
 1. 已先执行 sky_take_out.sql 导入苍穹外卖原始数据。
 2. 已存在推荐扩展表：dish_tag、user_food_preference、dish_sales_stat、recommend_session、recommend_result。

 设计说明：
 - 标签基于原有 dish/category/dish_flavor 数据生成，不凭空造菜。
 - 用户偏好基于原有用户 id=4 以及 order_detail 中已点过的菜品生成。
 - 销量统计模拟最近一周热度，实际订单中出现过的菜品权重更高。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM recommend_result;
DELETE FROM recommend_session;
DELETE FROM agent_call_log;
DELETE FROM user_food_preference;
DELETE FROM dish_sales_stat;
DELETE FROM dish_tag;

-- ----------------------------
-- 1. 菜品标签数据：dish_tag
-- ----------------------------

-- 酒水饮料
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '饮料', 'scene', 1.00, NOW() FROM dish WHERE name IN ('王老吉', '北冰洋', '雪花啤酒');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '解腻', 'scene', 0.80, NOW() FROM dish WHERE name IN ('王老吉', '北冰洋');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '下饭搭配', 'scene', 0.70, NOW() FROM dish WHERE name IN ('王老吉', '北冰洋', '雪花啤酒');

-- 主食
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '主食', 'scene', 1.00, NOW() FROM dish WHERE name IN ('米饭', '馒头');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '适合午餐', 'scene', 1.00, NOW() FROM dish WHERE name IN ('米饭', '馒头');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '清淡', 'taste', 0.80, NOW() FROM dish WHERE name IN ('米饭', '馒头');

-- 水煮鱼/酸菜鱼
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '鱼类', 'ingredient', 1.00, NOW() FROM dish WHERE name IN ('老坛酸菜鱼', '经典酸菜鮰鱼', '蜀味水煮草鱼', '剁椒鱼头', '清蒸鲈鱼', '草鱼2斤', '江团鱼2斤', '鮰鱼2斤');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '酸菜', 'taste', 1.00, NOW() FROM dish WHERE name IN ('老坛酸菜鱼', '经典酸菜鮰鱼', '金汤酸菜牛蛙');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '辣', 'taste', 1.00, NOW() FROM dish WHERE name IN ('蜀味水煮草鱼', '剁椒鱼头', '草鱼2斤', '江团鱼2斤', '鮰鱼2斤', '香锅牛蛙', '馋嘴牛蛙');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '重口味', 'taste', 1.00, NOW() FROM dish WHERE name IN ('蜀味水煮草鱼', '剁椒鱼头', '香锅牛蛙', '馋嘴牛蛙', '草鱼2斤', '江团鱼2斤', '鮰鱼2斤');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '适合聚餐', 'scene', 0.90, NOW() FROM dish WHERE name IN ('老坛酸菜鱼', '经典酸菜鮰鱼', '蜀味水煮草鱼', '金汤酸菜牛蛙', '香锅牛蛙', '馋嘴牛蛙', '草鱼2斤', '江团鱼2斤', '鮰鱼2斤');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '高蛋白', 'nutrition', 0.90, NOW() FROM dish WHERE name IN ('老坛酸菜鱼', '经典酸菜鮰鱼', '蜀味水煮草鱼', '清蒸鲈鱼', '剁椒鱼头', '金汤酸菜牛蛙', '香锅牛蛙', '馋嘴牛蛙');

-- 新鲜时蔬
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '蔬菜', 'ingredient', 1.00, NOW() FROM dish WHERE name IN ('清炒小油菜', '蒜蓉娃娃菜', '清炒西兰花', '炝炒圆白菜');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '清淡', 'taste', 1.00, NOW() FROM dish WHERE name IN ('清炒小油菜', '蒜蓉娃娃菜', '清炒西兰花');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '低脂', 'nutrition', 1.00, NOW() FROM dish WHERE name IN ('清炒小油菜', '蒜蓉娃娃菜', '清炒西兰花', '炝炒圆白菜');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '适合健身', 'scene', 0.90, NOW() FROM dish WHERE name IN ('清炒小油菜', '清炒西兰花', '炝炒圆白菜');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '家常', 'scene', 0.90, NOW() FROM dish WHERE name IN ('清炒小油菜', '蒜蓉娃娃菜', '清炒西兰花', '炝炒圆白菜');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '含蒜', 'ingredient', 0.80, NOW() FROM dish WHERE name = '蒜蓉娃娃菜';

-- 特色蒸菜
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '清淡', 'taste', 0.90, NOW() FROM dish WHERE name = '清蒸鲈鱼';
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '高蛋白', 'nutrition', 1.00, NOW() FROM dish WHERE name = '清蒸鲈鱼';
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '下饭', 'scene', 1.00, NOW() FROM dish WHERE name IN ('东坡肘子', '梅菜扣肉', '剁椒鱼头');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '家常', 'scene', 0.80, NOW() FROM dish WHERE name IN ('梅菜扣肉', '东坡肘子');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '猪肉', 'ingredient', 1.00, NOW() FROM dish WHERE name IN ('东坡肘子', '梅菜扣肉');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '高热量', 'nutrition', 0.80, NOW() FROM dish WHERE name IN ('东坡肘子', '梅菜扣肉');

-- 牛蛙/烤鱼
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '牛蛙', 'ingredient', 1.00, NOW() FROM dish WHERE name IN ('金汤酸菜牛蛙', '香锅牛蛙', '馋嘴牛蛙');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '下饭', 'scene', 1.00, NOW() FROM dish WHERE name IN ('香锅牛蛙', '馋嘴牛蛙', '草鱼2斤', '江团鱼2斤', '鮰鱼2斤', '蜀味水煮草鱼');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '含莲藕', 'ingredient', 0.60, NOW() FROM dish WHERE name IN ('香锅牛蛙', '草鱼2斤', '江团鱼2斤', '鮰鱼2斤');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '含豆芽', 'ingredient', 0.60, NOW() FROM dish WHERE name IN ('馋嘴牛蛙', '草鱼2斤', '江团鱼2斤', '鮰鱼2斤');

-- 汤类
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '汤类', 'scene', 1.00, NOW() FROM dish WHERE name IN ('鸡蛋汤', '平菇豆腐汤');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '清淡', 'taste', 1.00, NOW() FROM dish WHERE name IN ('鸡蛋汤', '平菇豆腐汤');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '适合午餐', 'scene', 0.90, NOW() FROM dish WHERE name IN ('鸡蛋汤', '平菇豆腐汤');
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '高蛋白', 'nutrition', 0.70, NOW() FROM dish WHERE name IN ('鸡蛋汤', '平菇豆腐汤');

-- 从原有 dish_flavor 的忌口数据补充通用标签
INSERT INTO dish_tag(dish_id, tag_name, tag_type, weight, create_time)
SELECT DISTINCT dish_id, '可选不要香菜', 'ingredient', 0.50, NOW()
FROM dish_flavor
WHERE value LIKE '%不要香菜%' AND dish_id IN (SELECT id FROM dish);

-- ----------------------------
-- 2. 用户偏好数据：user_food_preference
-- 用户 id=4 来自原始 user 表；偏好参考 order_detail 中已点菜品。
-- ----------------------------

INSERT INTO user_food_preference(user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT id, 'like', '牛蛙', 'history', 1.20, NOW(), NOW() FROM user WHERE id = 4;
INSERT INTO user_food_preference(user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT id, 'like', '酸菜', 'history', 1.00, NOW(), NOW() FROM user WHERE id = 4;
INSERT INTO user_food_preference(user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT id, 'like', '蔬菜', 'history', 0.90, NOW(), NOW() FROM user WHERE id = 4;
INSERT INTO user_food_preference(user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT id, 'like', '汤类', 'history', 0.80, NOW(), NOW() FROM user WHERE id = 4;
INSERT INTO user_food_preference(user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT id, 'dislike', '葱', 'history', 1.00, NOW(), NOW() FROM user WHERE id = 4;
INSERT INTO user_food_preference(user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT id, 'taste', '不辣', 'history', 0.80, NOW(), NOW() FROM user WHERE id = 4;

-- ----------------------------
-- 3. 销量统计数据：dish_sales_stat
-- 实际订单出现的菜品给更高热度，其余按菜品类型模拟合理热度。
-- ----------------------------

INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 35, '2026-04-03', 'day' FROM dish WHERE name = '馋嘴牛蛙';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 30, '2026-04-03', 'day' FROM dish WHERE name = '经典酸菜鮰鱼';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 24, '2026-04-03', 'day' FROM dish WHERE name = '炝炒圆白菜';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 22, '2026-04-03', 'day' FROM dish WHERE name = '鸡蛋汤';

INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 28, '2026-04-04', 'day' FROM dish WHERE name = '蜀味水煮草鱼';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 26, '2026-04-04', 'day' FROM dish WHERE name = '米饭';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 25, '2026-04-04', 'day' FROM dish WHERE name = '老坛酸菜鱼';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 20, '2026-04-04', 'day' FROM dish WHERE name = '清炒西兰花';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 18, '2026-04-04', 'day' FROM dish WHERE name = '王老吉';

INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 31, '2026-04-05', 'day' FROM dish WHERE name = '香锅牛蛙';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 29, '2026-04-05', 'day' FROM dish WHERE name = '剁椒鱼头';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 23, '2026-04-05', 'day' FROM dish WHERE name = '平菇豆腐汤';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 21, '2026-04-05', 'day' FROM dish WHERE name = '蒜蓉娃娃菜';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 16, '2026-04-05', 'day' FROM dish WHERE name = '北冰洋';

INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 27, '2026-04-06', 'day' FROM dish WHERE name = '金汤酸菜牛蛙';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 26, '2026-04-06', 'day' FROM dish WHERE name = '草鱼2斤';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 19, '2026-04-06', 'day' FROM dish WHERE name = '清炒小油菜';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 17, '2026-04-06', 'day' FROM dish WHERE name = '馒头';

INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 25, '2026-04-07', 'day' FROM dish WHERE name = '鮰鱼2斤';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 20, '2026-04-07', 'day' FROM dish WHERE name = '梅菜扣肉';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 14, '2026-04-07', 'day' FROM dish WHERE name = '雪花啤酒';
INSERT INTO dish_sales_stat(dish_id, sales_count, stat_date, stat_type)
SELECT id, 12, '2026-04-07', 'day' FROM dish WHERE name = '清蒸鲈鱼';

-- ----------------------------
-- 4. 推荐会话与结果样例：recommend_session / recommend_result
-- 用于演示“推荐链路可追踪”。
-- ----------------------------

INSERT INTO recommend_session(user_id, user_query, parsed_intent, create_time)
VALUES (
  4,
  '我今天想吃辣一点的，最好适合下饭',
  JSON_OBJECT('tasteTags', JSON_ARRAY('辣'), 'sceneTags', JSON_ARRAY('下饭'), 'excludeTags', JSON_ARRAY(), 'mealType', NULL, 'budget', NULL),
  NOW()
);
SET @session_spicy := LAST_INSERT_ID();

INSERT INTO recommend_result(session_id, dish_id, setmeal_id, score, reason, rank_no, create_time)
SELECT @session_spicy, id, NULL, 92.50, '命中辣味和下饭标签，销量热度较高，适合重口味用餐场景', 1, NOW()
FROM dish WHERE name = '馋嘴牛蛙';
INSERT INTO recommend_result(session_id, dish_id, setmeal_id, score, reason, rank_no, create_time)
SELECT @session_spicy, id, NULL, 88.00, '口味偏辣，适合搭配米饭，近期销量表现较好', 2, NOW()
FROM dish WHERE name = '蜀味水煮草鱼';
INSERT INTO recommend_result(session_id, dish_id, setmeal_id, score, reason, rank_no, create_time)
SELECT @session_spicy, id, NULL, 84.00, '牛蛙类菜品符合用户历史偏好，口味重且适合聚餐', 3, NOW()
FROM dish WHERE name = '香锅牛蛙';

INSERT INTO recommend_session(user_id, user_query, parsed_intent, create_time)
VALUES (
  4,
  '我想吃清淡点，预算20元以内',
  JSON_OBJECT('tasteTags', JSON_ARRAY('清淡'), 'sceneTags', JSON_ARRAY(), 'excludeTags', JSON_ARRAY(), 'mealType', NULL, 'budget', 20),
  NOW()
);
SET @session_light := LAST_INSERT_ID();

INSERT INTO recommend_result(session_id, dish_id, setmeal_id, score, reason, rank_no, create_time)
SELECT @session_light, id, NULL, 91.00, '清淡低脂，价格在20元预算内，适合日常用餐', 1, NOW()
FROM dish WHERE name = '清炒西兰花';
INSERT INTO recommend_result(session_id, dish_id, setmeal_id, score, reason, rank_no, create_time)
SELECT @session_light, id, NULL, 88.50, '蔬菜类菜品符合用户历史偏好，价格在预算内', 2, NOW()
FROM dish WHERE name = '清炒小油菜';
INSERT INTO recommend_result(session_id, dish_id, setmeal_id, score, reason, rank_no, create_time)
SELECT @session_light, id, NULL, 82.00, '汤类清淡，价格低，适合作为轻食搭配', 3, NOW()
FROM dish WHERE name = '鸡蛋汤';

SET FOREIGN_KEY_CHECKS = 1;
