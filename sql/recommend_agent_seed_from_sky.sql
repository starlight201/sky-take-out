-- 智能推荐扩展表初始化数据
-- 数据来源：sky_take_out.sql 中已有的 category、dish、dish_flavor、orders、order_detail、user。
-- 原则：只写能从现有事实推导的数据；不能确定的用户偏好、推荐会话、推荐结果、工具日志不造假。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. 菜品标签：从分类事实推导
-- ---------------------------------------------------------------------

-- 分类 11：酒水饮料
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '饮品', 'category', 1.00, NOW()
FROM dish
WHERE category_id = 11;

-- 分类 12：传统主食
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '主食', 'category', 1.00, NOW()
FROM dish
WHERE category_id = 12;

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '适合午餐', 'scene', 0.80, NOW()
FROM dish
WHERE category_id = 12;

-- 分类 16、17、20：蜀味烤鱼、蜀味牛蛙、水煮鱼，属于菜品分类事实，不等同于库存或销量
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '川味', 'taste', 0.80, NOW()
FROM dish
WHERE category_id IN (16, 17, 20);

-- 分类 18：特色蒸菜
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '蒸菜', 'category', 1.00, NOW()
FROM dish
WHERE category_id = 18;

-- 分类 19：新鲜时蔬
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '蔬菜', 'category', 1.00, NOW()
FROM dish
WHERE category_id = 19;

-- 分类 21：汤类
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '汤', 'category', 1.00, NOW()
FROM dish
WHERE category_id = 21;

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '适合搭配', 'scene', 0.80, NOW()
FROM dish
WHERE category_id = 21;

-- ---------------------------------------------------------------------
-- 2. 菜品标签：从 dish_flavor 口味配置推导
-- 说明：有“辣度”配置的菜品，只说明支持辣度选择，因此标记“辣/微辣/重辣/可选辣度”。
-- ---------------------------------------------------------------------

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT DISTINCT dish_id, '可选辣度', 'taste', 1.00, NOW()
FROM dish_flavor
WHERE name = '辣度';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT DISTINCT dish_id, '辣', 'taste', 1.00, NOW()
FROM dish_flavor
WHERE name = '辣度'
  AND value LIKE '%中辣%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT DISTINCT dish_id, '微辣', 'taste', 0.90, NOW()
FROM dish_flavor
WHERE name = '辣度'
  AND value LIKE '%微辣%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT DISTINCT dish_id, '重辣', 'taste', 0.90, NOW()
FROM dish_flavor
WHERE name = '辣度'
  AND value LIKE '%重辣%';

-- 有“甜味”配置只说明可选甜度，不代表菜品一定是甜口。
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT DISTINCT dish_id, '可选甜度', 'taste', 0.80, NOW()
FROM dish_flavor
WHERE name = '甜味';

-- ---------------------------------------------------------------------
-- 3. 菜品标签：从菜品名称、描述中的原料/配料事实推导
-- ---------------------------------------------------------------------

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '鱼', 'ingredient', 1.00, NOW()
FROM dish
WHERE name LIKE '%鱼%' OR description LIKE '%鱼%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '草鱼', 'ingredient', 1.00, NOW()
FROM dish
WHERE name LIKE '%草鱼%' OR description LIKE '%草鱼%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '酸菜', 'ingredient', 1.00, NOW()
FROM dish
WHERE name LIKE '%酸菜%' OR description LIKE '%酸菜%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '牛蛙', 'ingredient', 1.00, NOW()
FROM dish
WHERE name LIKE '%牛蛙%' OR description LIKE '%牛蛙%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '猪肉', 'ingredient', 1.00, NOW()
FROM dish
WHERE description LIKE '%猪肉%' OR name LIKE '%肉%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '鸡蛋', 'ingredient', 1.00, NOW()
FROM dish
WHERE name LIKE '%鸡蛋%' OR description LIKE '%鸡蛋%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '豆腐', 'ingredient', 1.00, NOW()
FROM dish
WHERE name LIKE '%豆腐%' OR description LIKE '%豆腐%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '菌菇', 'ingredient', 1.00, NOW()
FROM dish
WHERE name LIKE '%菇%' OR description LIKE '%菇%';

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '蔬菜', 'ingredient', 1.00, NOW()
FROM dish
WHERE name LIKE '%油菜%'
   OR name LIKE '%娃娃菜%'
   OR name LIKE '%西兰花%'
   OR name LIKE '%圆白菜%'
   OR description LIKE '%小油菜%'
   OR description LIKE '%娃娃菜%'
   OR description LIKE '%西兰花%'
   OR description LIKE '%圆白菜%';

-- 从菜名“清炒/清蒸”和“汤类”推导清淡倾向；这来自命名事实，不表示营养承诺。
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '清淡', 'taste', 0.80, NOW()
FROM dish
WHERE name LIKE '清炒%'
   OR name LIKE '清蒸%'
   OR category_id = 21;

-- 主食、鱼、牛蛙、肉、蛋、豆腐可作为常见正餐搭配候选。
INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time)
SELECT id, '适合午餐', 'scene', 0.70, NOW()
FROM dish
WHERE category_id IN (12, 16, 17, 18, 20, 21);

-- ---------------------------------------------------------------------
-- 3.1 菜品标签：基于 sky_take_out.sql 中 dish 固定 ID 的确定事实补充
-- 说明：这段不依赖中文 LIKE 匹配，避免导入环境字符集显示异常时标签缺失。
-- ---------------------------------------------------------------------

INSERT IGNORE INTO dish_tag (dish_id, tag_name, tag_type, weight, create_time) VALUES
(51, '鱼', 'ingredient', 1.00, NOW()),
(51, '草鱼', 'ingredient', 1.00, NOW()),
(51, '酸菜', 'ingredient', 1.00, NOW()),
(52, '鱼', 'ingredient', 1.00, NOW()),
(52, '酸菜', 'ingredient', 1.00, NOW()),
(53, '鱼', 'ingredient', 1.00, NOW()),
(53, '草鱼', 'ingredient', 1.00, NOW()),
(54, '蔬菜', 'ingredient', 1.00, NOW()),
(54, '清淡', 'taste', 0.80, NOW()),
(55, '蔬菜', 'ingredient', 1.00, NOW()),
(55, '蒜', 'ingredient', 1.00, NOW()),
(56, '蔬菜', 'ingredient', 1.00, NOW()),
(56, '清淡', 'taste', 0.80, NOW()),
(57, '蔬菜', 'ingredient', 1.00, NOW()),
(58, '鱼', 'ingredient', 1.00, NOW()),
(58, '清淡', 'taste', 0.80, NOW()),
(59, '猪肉', 'ingredient', 1.00, NOW()),
(60, '猪肉', 'ingredient', 1.00, NOW()),
(60, '梅菜', 'ingredient', 1.00, NOW()),
(61, '鱼', 'ingredient', 1.00, NOW()),
(61, '剁椒', 'ingredient', 1.00, NOW()),
(62, '牛蛙', 'ingredient', 1.00, NOW()),
(62, '酸菜', 'ingredient', 1.00, NOW()),
(63, '牛蛙', 'ingredient', 1.00, NOW()),
(63, '莲藕', 'ingredient', 1.00, NOW()),
(63, '青笋', 'ingredient', 1.00, NOW()),
(64, '牛蛙', 'ingredient', 1.00, NOW()),
(64, '丝瓜', 'ingredient', 1.00, NOW()),
(64, '黄豆芽', 'ingredient', 1.00, NOW()),
(65, '鱼', 'ingredient', 1.00, NOW()),
(65, '草鱼', 'ingredient', 1.00, NOW()),
(65, '黄豆芽', 'ingredient', 1.00, NOW()),
(65, '莲藕', 'ingredient', 1.00, NOW()),
(66, '鱼', 'ingredient', 1.00, NOW()),
(66, '江团鱼', 'ingredient', 1.00, NOW()),
(66, '黄豆芽', 'ingredient', 1.00, NOW()),
(66, '莲藕', 'ingredient', 1.00, NOW()),
(67, '鱼', 'ingredient', 1.00, NOW()),
(67, '鮰鱼', 'ingredient', 1.00, NOW()),
(67, '黄豆芽', 'ingredient', 1.00, NOW()),
(67, '莲藕', 'ingredient', 1.00, NOW()),
(68, '鸡蛋', 'ingredient', 1.00, NOW()),
(68, '紫菜', 'ingredient', 1.00, NOW()),
(68, '汤', 'category', 1.00, NOW()),
(68, '清淡', 'taste', 0.80, NOW()),
(69, '豆腐', 'ingredient', 1.00, NOW()),
(69, '菌菇', 'ingredient', 1.00, NOW()),
(69, '汤', 'category', 1.00, NOW()),
(69, '清淡', 'taste', 0.80, NOW());

-- ---------------------------------------------------------------------
-- 4. 菜品销量统计：只从“已完成订单(status=5)”聚合
-- 当前 sky_take_out.sql 中订单 id=4 的 status=1（待付款），因此本脚本不会把它计入销量。
-- 如果后续有已完成订单，重复执行该段会按真实订单生成/更新销量。
-- ---------------------------------------------------------------------

INSERT INTO dish_sales_stat (dish_id, sales_count, stat_date, stat_type)
SELECT
    od.dish_id,
    SUM(od.number) AS sales_count,
    DATE(o.order_time) AS stat_date,
    'day' AS stat_type
FROM orders o
JOIN order_detail od ON o.id = od.order_id
WHERE o.status = 5
  AND od.dish_id IS NOT NULL
GROUP BY od.dish_id, DATE(o.order_time)
ON DUPLICATE KEY UPDATE
    sales_count = VALUES(sales_count);

-- ---------------------------------------------------------------------
-- 5. 用户饮食偏好：只从已完成订单中的明确口味选择推导
-- 当前只有待付款订单，因此不会插入历史偏好，避免把未完成订单当成用户偏好。
-- ---------------------------------------------------------------------

INSERT INTO user_food_preference (user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT DISTINCT o.user_id, 'dislike', '辣', 'history', 0.80, NOW(), NOW()
FROM orders o
JOIN order_detail od ON o.id = od.order_id
WHERE o.status = 5
  AND od.dish_flavor LIKE '%不辣%'
  AND NOT EXISTS (
      SELECT 1
      FROM user_food_preference p
      WHERE p.user_id = o.user_id
        AND p.preference_type = 'dislike'
        AND p.preference_value = '辣'
  );

INSERT INTO user_food_preference (user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT DISTINCT o.user_id, 'dislike', '葱', 'history', 0.80, NOW(), NOW()
FROM orders o
JOIN order_detail od ON o.id = od.order_id
WHERE o.status = 5
  AND od.dish_flavor LIKE '%不要葱%'
  AND NOT EXISTS (
      SELECT 1
      FROM user_food_preference p
      WHERE p.user_id = o.user_id
        AND p.preference_type = 'dislike'
        AND p.preference_value = '葱'
  );

INSERT INTO user_food_preference (user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT DISTINCT o.user_id, 'dislike', '蒜', 'history', 0.80, NOW(), NOW()
FROM orders o
JOIN order_detail od ON o.id = od.order_id
WHERE o.status = 5
  AND od.dish_flavor LIKE '%不要蒜%'
  AND NOT EXISTS (
      SELECT 1
      FROM user_food_preference p
      WHERE p.user_id = o.user_id
        AND p.preference_type = 'dislike'
        AND p.preference_value = '蒜'
  );

INSERT INTO user_food_preference (user_id, preference_type, preference_value, source, weight, create_time, update_time)
SELECT DISTINCT o.user_id, 'dislike', '香菜', 'history', 0.80, NOW(), NOW()
FROM orders o
JOIN order_detail od ON o.id = od.order_id
WHERE o.status = 5
  AND od.dish_flavor LIKE '%不要香菜%'
  AND NOT EXISTS (
      SELECT 1
      FROM user_food_preference p
      WHERE p.user_id = o.user_id
        AND p.preference_type = 'dislike'
        AND p.preference_value = '香菜'
  );

-- ---------------------------------------------------------------------
-- 6. 推荐会话、推荐结果、工具调用日志
-- 这些表记录真实推荐运行过程，sky_take_out.sql 中没有历史推荐行为，因此不插入模拟数据。
-- ---------------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;
