-- 菜品库存表
-- 使用 version 字段实现乐观锁，避免并发下单超卖。

CREATE TABLE IF NOT EXISTS dish_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dish_stock_dish_id (dish_id),
    KEY idx_dish_stock_stock (stock)
) COMMENT '菜品库存表';

-- 为已有菜品初始化库存。默认 100 只是初始运营值，可在后台接口调整。
INSERT INTO dish_stock (dish_id, stock, version, create_time, update_time)
SELECT id, 100, 0, NOW(), NOW()
FROM dish
ON DUPLICATE KEY UPDATE dish_id = dish_id;
