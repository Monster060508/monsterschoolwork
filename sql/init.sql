-- 企业销售管理系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS sales_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sales_management;

-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL COMMENT '角色(ADMIN/SALES)',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    photo_url VARCHAR(500) COMMENT '照片URL',
    status INT DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
    INDEX idx_name (name),
    INDEX idx_price (price),
    INDEX idx_stock (stock_quantity),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态(PENDING/IN_PROGRESS/COMPLETED/CANCELLED)',
    salesperson_id BIGINT NOT NULL COMMENT '销售人员ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
    INDEX idx_order_no (order_no),
    INDEX idx_customer (customer_name),
    INDEX idx_status (status),
    INDEX idx_salesperson (salesperson_id),
    INDEX idx_create_time (create_time),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (salesperson_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 创建订单项表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- 创建订单状态变更日志表
CREATE TABLE IF NOT EXISTS order_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    old_status VARCHAR(20) COMMENT '原状态',
    new_status VARCHAR(20) NOT NULL COMMENT '新状态',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_operator_id (operator_id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态变更日志表';

-- 创建库存变更日志表
CREATE TABLE IF NOT EXISTS stock_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型(ORDER_COMPLETE/ORDER_CANCEL/MANUAL_ADJUST)',
    change_quantity INT NOT NULL COMMENT '变更数量(正数增加，负数减少)',
    before_quantity INT NOT NULL COMMENT '变更前库存',
    after_quantity INT NOT NULL COMMENT '变更后库存',
    order_id BIGINT COMMENT '关联订单ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_product_id (product_id),
    INDEX idx_order_id (order_id),
    INDEX idx_operator_id (operator_id),
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变更日志表';

-- 创建销售统计数据表（用于缓存统计数据）
CREATE TABLE IF NOT EXISTS sales_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_sales DECIMAL(12,2) DEFAULT 0 COMMENT '总销售额',
    order_count INT DEFAULT 0 COMMENT '订单数量',
    completed_order_count INT DEFAULT 0 COMMENT '已完成订单数量',
    cancelled_order_count INT DEFAULT 0 COMMENT '已取消订单数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售统计数据表';

-- 创建销售员日报表
CREATE TABLE IF NOT EXISTS salesperson_daily_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报表ID',
    salesperson_id BIGINT NOT NULL COMMENT '销售人员ID',
    report_date DATE NOT NULL COMMENT '报表日期',
    daily_sales DECIMAL(12,2) DEFAULT 0 COMMENT '当日销售额',
    order_count INT DEFAULT 0 COMMENT '当日订单数',
    completed_order_count INT DEFAULT 0 COMMENT '当日完成订单数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_salesperson_date (salesperson_id, report_date),
    FOREIGN KEY (salesperson_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售员日报表';

-- 创建商品销售统计表
CREATE TABLE IF NOT EXISTS product_sales_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    sales_quantity INT DEFAULT 0 COMMENT '销售数量',
    sales_amount DECIMAL(12,2) DEFAULT 0 COMMENT '销售金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_product_date (product_id, stat_date),
    FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品销售统计表';

-- 创建对话历史表
CREATE TABLE IF NOT EXISTS conversation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '对话ID',
    conversation_id VARCHAR(100) NOT NULL COMMENT '对话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL COMMENT '角色(user/assistant)',
    content TEXT NOT NULL COMMENT '对话内容',
    intent VARCHAR(50) COMMENT '意图分类',
    sql_query TEXT COMMENT '生成的SQL查询',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话历史表';

-- 创建向量文档表（用于RAG）
CREATE TABLE IF NOT EXISTS vector_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    title VARCHAR(200) NOT NULL COMMENT '文档标题',
    content TEXT NOT NULL COMMENT '文档内容',
    embedding VECTOR(1536) COMMENT '向量嵌入',
    metadata JSON COMMENT '元数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='向量文档表';

-- 创建Markdown文档表（用于RAG）
CREATE TABLE IF NOT EXISTS markdown_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    title VARCHAR(200) NOT NULL COMMENT '文档标题',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名',
    oss_url VARCHAR(500) COMMENT 'OSS文件URL',
    content TEXT COMMENT '文档内容',
    file_size BIGINT COMMENT '文件大小',
    upload_user_id BIGINT COMMENT '上传用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除(0:未删除,1:已删除)',
    INDEX idx_title (title),
    INDEX idx_file_name (file_name),
    INDEX idx_upload_user_id (upload_user_id),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (upload_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Markdown文档表';

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 创建登录日志表
CREATE TABLE IF NOT EXISTS login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status VARCHAR(20) NOT NULL COMMENT '登录状态(SUCCESS/FAIL)',
    message VARCHAR(500) COMMENT '消息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 创建文件上传记录表
CREATE TABLE IF NOT EXISTS file_upload_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名',
    original_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小',
    file_type VARCHAR(50) COMMENT '文件类型',
    upload_user_id BIGINT COMMENT '上传用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_upload_user_id (upload_user_id),
    INDEX idx_file_type (file_type),
    FOREIGN KEY (upload_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传记录表';

-- 创建数据字典表
CREATE TABLE IF NOT EXISTS data_dictionary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典ID',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(100) NOT NULL COMMENT '字典编码',
    dict_value VARCHAR(200) NOT NULL COMMENT '字典值',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_type_code (dict_type, dict_code),
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 创建定时任务表
CREATE TABLE IF NOT EXISTS scheduled_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_group VARCHAR(50) NOT NULL COMMENT '任务分组',
    cron_expression VARCHAR(50) NOT NULL COMMENT 'cron表达式',
    invoke_target VARCHAR(500) NOT NULL COMMENT '调用目标',
    status VARCHAR(20) DEFAULT 'RUNNING' COMMENT '任务状态(RUNNING/PAUSED)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_group (task_group),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务表';

-- 创建索引优化查询性能
CREATE INDEX idx_orders_create_time_status ON orders(create_time, status);
CREATE INDEX idx_orders_salesperson_status ON orders(salesperson_id, status);
CREATE INDEX idx_order_item_order_product ON order_item(order_id, product_id);
CREATE INDEX idx_product_stock_price ON product(stock_quantity, price);
CREATE INDEX idx_user_role_deleted ON sys_user(role, deleted);

-- 创建视图简化查询
CREATE OR REPLACE VIEW v_order_detail AS
SELECT 
    o.id,
    o.order_no,
    o.customer_name,
    o.total_amount,
    o.status,
    o.salesperson_id,
    u.name AS salesperson_name,
    u.photo_url AS salesperson_photo,
    o.create_time,
    o.update_time
FROM orders o
LEFT JOIN sys_user u ON o.salesperson_id = u.id
WHERE o.deleted = 0;

CREATE OR REPLACE VIEW v_order_item_detail AS
SELECT 
    oi.id,
    oi.order_id,
    oi.product_id,
    p.name AS product_name,
    p.image_url AS product_image,
    oi.quantity,
    oi.unit_price,
    (oi.quantity * oi.unit_price) AS subtotal
FROM order_item oi
LEFT JOIN product p ON oi.product_id = p.id;

CREATE OR REPLACE VIEW v_sales_ranking AS
SELECT 
    u.id AS salesperson_id,
    u.name AS salesperson_name,
    u.photo_url,
    COUNT(o.id) AS order_count,
    SUM(o.total_amount) AS total_sales,
    AVG(o.total_amount) AS avg_order_amount
FROM sys_user u
LEFT JOIN orders o ON u.id = o.salesperson_id AND o.status = 'COMPLETED'
WHERE u.role = 'SALES' AND u.deleted = 0
GROUP BY u.id, u.name, u.photo_url
ORDER BY total_sales DESC;

CREATE OR REPLACE VIEW v_product_sales_ranking AS
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    p.image_url,
    p.price,
    SUM(oi.quantity) AS sales_quantity,
    SUM(oi.quantity * oi.unit_price) AS sales_amount
FROM product p
LEFT JOIN order_item oi ON p.id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'COMPLETED'
WHERE p.deleted = 0
GROUP BY p.id, p.name, p.image_url, p.price
ORDER BY sales_quantity DESC;

-- 创建存储过程用于统计
DELIMITER //

CREATE PROCEDURE sp_update_daily_statistics(IN stat_date DATE)
BEGIN
    DECLARE total_sales DECIMAL(12,2) DEFAULT 0;
    DECLARE order_count INT DEFAULT 0;
    DECLARE completed_count INT DEFAULT 0;
    DECLARE cancelled_count INT DEFAULT 0;
    
    -- 获取当日统计数据
    SELECT 
        COALESCE(SUM(total_amount), 0),
        COUNT(*),
        SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END)
    INTO total_sales, order_count, completed_count, cancelled_count
    FROM orders 
    WHERE DATE(create_time) = stat_date AND deleted = 0;
    
    -- 更新或插入统计数据
    INSERT INTO sales_statistics (stat_date, total_sales, order_count, completed_order_count, cancelled_order_count)
    VALUES (stat_date, total_sales, order_count, completed_count, cancelled_count)
    ON DUPLICATE KEY UPDATE
        total_sales = VALUES(total_sales),
        order_count = VALUES(order_count),
        completed_order_count = VALUES(completed_order_count),
        cancelled_order_count = VALUES(cancelled_order_count);
END //

CREATE PROCEDURE sp_update_salesperson_daily_report(IN report_date DATE)
BEGIN
    -- 删除当日旧数据
    DELETE FROM salesperson_daily_report WHERE report_date = report_date;
    
    -- 插入当日新数据
    INSERT INTO salesperson_daily_report (salesperson_id, report_date, daily_sales, order_count, completed_order_count)
    SELECT 
        u.id,
        report_date,
        COALESCE(SUM(CASE WHEN DATE(o.create_time) = report_date THEN o.total_amount ELSE 0 END), 0),
        COUNT(CASE WHEN DATE(o.create_time) = report_date THEN 1 END),
        COUNT(CASE WHEN DATE(o.create_time) = report_date AND o.status = 'COMPLETED' THEN 1 END)
    FROM sys_user u
    LEFT JOIN orders o ON u.id = o.salesperson_id AND o.deleted = 0
    WHERE u.role = 'SALES' AND u.deleted = 0
    GROUP BY u.id;
END //

CREATE PROCEDURE sp_update_product_sales_statistics(IN stat_date DATE)
BEGIN
    -- 删除当日旧数据
    DELETE FROM product_sales_statistics WHERE stat_date = stat_date;
    
    -- 插入当日新数据
    INSERT INTO product_sales_statistics (product_id, stat_date, sales_quantity, sales_amount)
    SELECT 
        p.id,
        stat_date,
        COALESCE(SUM(CASE WHEN DATE(o.create_time) = stat_date THEN oi.quantity ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN DATE(o.create_time) = stat_date THEN oi.quantity * oi.unit_price ELSE 0 END), 0)
    FROM product p
    LEFT JOIN order_item oi ON p.id = oi.product_id
    LEFT JOIN orders o ON oi.order_id = o.id AND o.deleted = 0
    WHERE p.deleted = 0
    GROUP BY p.id;
END //

DELIMITER ;

-- 创建触发器自动更新统计
DELIMITER //

CREATE TRIGGER trg_after_order_insert
AFTER INSERT ON orders
FOR EACH ROW
BEGIN
    -- 更新当日销售统计
    CALL sp_update_daily_statistics(DATE(NEW.create_time));
    -- 更新销售人员日报
    CALL sp_update_salesperson_daily_report(DATE(NEW.create_time));
END //

CREATE TRIGGER trg_after_order_update
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    -- 如果订单完成，扣减库存
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        -- 这里会在应用层处理库存扣减
        -- 记录库存变更日志
        INSERT INTO stock_change_log (product_id, change_type, change_quantity, before_quantity, after_quantity, order_id, operator_id, remark)
        SELECT 
            oi.product_id,
            'ORDER_COMPLETE',
            -oi.quantity,
            p.stock_quantity,
            p.stock_quantity - oi.quantity,
            NEW.id,
            NEW.salesperson_id,
            CONCAT('订单完成扣减库存，订单号：', NEW.order_no)
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        WHERE oi.order_id = NEW.id;
        
        -- 更新商品库存
        UPDATE product p
        JOIN order_item oi ON p.id = oi.product_id
        SET p.stock_quantity = p.stock_quantity - oi.quantity
        WHERE oi.order_id = NEW.id;
    END IF;
    
    -- 更新当日销售统计
    CALL sp_update_daily_statistics(DATE(NEW.create_time));
    -- 更新销售人员日报
    CALL sp_update_salesperson_daily_report(DATE(NEW.create_time));
    -- 更新商品销售统计
    CALL sp_update_product_sales_statistics(DATE(NEW.create_time));
END //

DELIMITER ;

-- 创建事件调度器（可选）
-- SET GLOBAL event_scheduler = ON;

-- 创建每日统计事件
-- CREATE EVENT IF NOT EXISTS evt_daily_statistics
-- ON SCHEDULE EVERY 1 DAY
-- STARTS CURRENT_DATE + INTERVAL 1 DAY
-- DO
-- BEGIN
--     CALL sp_update_daily_statistics(CURRENT_DATE - INTERVAL 1 DAY);
--     CALL sp_update_salesperson_daily_report(CURRENT_DATE - INTERVAL 1 DAY);
--     CALL sp_update_product_sales_statistics(CURRENT_DATE - INTERVAL 1 DAY);
-- END;

-- 完成初始化
SELECT '数据库初始化完成' AS message;