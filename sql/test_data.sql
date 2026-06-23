-- 企业销售管理系统测试数据脚本
USE sales_management;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 清空现有数据
DELETE FROM order_item;
DELETE FROM orders;
DELETE FROM product;
DELETE FROM sys_user;
DELETE FROM conversation_history;
DELETE FROM salesperson_daily_report;
DELETE FROM product_sales_statistics;
DELETE FROM sales_statistics;

-- 重置自增ID
ALTER TABLE order_item AUTO_INCREMENT = 1;
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE product AUTO_INCREMENT = 1;
ALTER TABLE sys_user AUTO_INCREMENT = 1;

-- 插入用户数据（密码使用BCrypt加密，原始密码为admin123和sales123）
INSERT INTO sys_user (username, password, role, name, phone, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', '系统管理员', '13800138000', 'admin@company.com', 1),
('sales001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SALES', '张三', '13800138001', 'zhangsan@company.com', 1),
('sales002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SALES', '李四', '13800138002', 'lisi@company.com', 1),
('sales003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SALES', '王五', '13800138003', 'wangwu@company.com', 1),
('sales004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SALES', '赵六', '13800138004', 'zhaoliu@company.com', 1),
('sales005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SALES', '钱七', '13800138005', 'qianqi@company.com', 1);

-- 插入商品数据
INSERT INTO product (name, description, price, stock_quantity, image_url) VALUES
('iPhone 15 Pro', '苹果最新旗舰手机，A17 Pro芯片，钛金属设计', 8999.00, 100, 'https://example.com/iphone15pro.jpg'),
('MacBook Pro 14', 'M3 Pro芯片，18GB内存，512GB存储', 14999.00, 50, 'https://example.com/macbookpro14.jpg'),
('AirPods Pro 2', '主动降噪，自适应音频，USB-C充电', 1899.00, 200, 'https://example.com/airpodspro2.jpg'),
('iPad Air 5', 'M1芯片，10.9英寸，256GB', 5499.00, 80, 'https://example.com/ipadair5.jpg'),
('Apple Watch Series 9', 'S9芯片，双指互点手势，血氧检测', 3299.00, 120, 'https://example.com/applewatch9.jpg'),
('Samsung Galaxy S24 Ultra', '骁龙8 Gen3，2亿像素，S Pen', 9999.00, 60, 'https://example.com/galaxys24ultra.jpg'),
('华为 Mate 60 Pro', '麒麟9000S，卫星通话，XMAGE影像', 6999.00, 90, 'https://example.com/mate60pro.jpg'),
('小米 14 Pro', '骁龙8 Gen3，徕卡光学，120W快充', 4999.00, 150, 'https://example.com/xiaomi14pro.jpg'),
('戴尔 XPS 15', 'i7-13700H，RTX 4060，3.5K OLED屏', 12999.00, 40, 'https://example.com/dellxps15.jpg'),
('索尼 WH-1000XM5', '旗舰降噪耳机，30小时续航', 2499.00, 180, 'https://example.com/sonyxm5.jpg'),
('Nintendo Switch OLED', '7英寸OLED屏幕，64GB存储', 2599.00, 100, 'https://example.com/switcholed.jpg'),
('DJI Mini 4 Pro', '249g轻巧机身，4K/60fps，避障', 4788.00, 70, 'https://example.com/djimini4pro.jpg');

-- 插入订单数据
INSERT INTO orders (order_no, customer_name, total_amount, status, salesperson_id) VALUES
('ORD20260601001', '北京科技有限公司', 89990.00, 'COMPLETED', 2),
('ORD20260601002', '上海贸易有限公司', 44997.00, 'COMPLETED', 3),
('ORD20260602001', '深圳电子科技公司', 149990.00, 'IN_PROGRESS', 2),
('ORD20260602002', '广州网络有限公司', 37980.00, 'PENDING', 4),
('ORD20260603001', '杭州软件有限公司', 26995.00, 'COMPLETED', 5),
('ORD20260603002', '成都创新科技公司', 59994.00, 'COMPLETED', 3),
('ORD20260604001', '武汉数据有限公司', 18990.00, 'CANCELLED', 6),
('ORD20260604002', '南京信息技术公司', 74995.00, 'IN_PROGRESS', 2),
('ORD20260605001', '天津制造有限公司', 32990.00, 'COMPLETED', 4),
('ORD20260605002', '重庆智能科技公司', 49990.00, 'PENDING', 5),
('ORD20260606001', '西安电子有限公司', 22497.00, 'COMPLETED', 6),
('ORD20260606002', '长沙软件开发公司', 64993.00, 'IN_PROGRESS', 3),
('ORD20260607001', '郑州网络技术公司', 15996.00, 'COMPLETED', 2),
('ORD20260607002', '合肥人工智能公司', 89991.00, 'PENDING', 4),
('ORD20260608001', '济南云计算公司', 29992.00, 'COMPLETED', 5);

-- 插入订单项数据
INSERT INTO order_item (order_id, product_id, quantity, unit_price, subtotal) VALUES
-- 订单1：北京科技有限公司
(1, 1, 10, 8999.00, 89990.00),
-- 订单2：上海贸易有限公司
(2, 2, 3, 14999.00, 44997.00),
-- 订单3：深圳电子科技公司
(3, 1, 10, 8999.00, 89990.00),
(3, 2, 4, 14999.00, 59996.00),
-- 订单4：广州网络有限公司
(4, 3, 20, 1899.00, 37980.00),
-- 订单5：杭州软件有限公司
(5, 4, 5, 5499.00, 27495.00),
-- 订单6：成都创新科技公司
(6, 6, 6, 9999.00, 59994.00),
-- 订单7：武汉数据有限公司
(7, 5, 6, 3299.00, 19794.00),  -- 注意：订单被取消
-- 订单8：南京信息技术公司
(8, 1, 5, 8999.00, 44995.00),
(8, 9, 2, 12999.00, 25998.00),
-- 订单9：天津制造有限公司
(9, 7, 4, 6999.00, 27996.00),
(9, 10, 2, 2499.00, 4998.00),
-- 订单10：重庆智能科技公司
(10, 8, 10, 4999.00, 49990.00),
-- 订单11：西安电子有限公司
(11, 3, 10, 1899.00, 18990.00),
(11, 11, 1, 2599.00, 2599.00),
-- 订单12：长沙软件开发公司
(12, 2, 4, 14999.00, 59996.00),
(12, 12, 1, 4788.00, 4788.00),
-- 订单13：郑州网络技术公司
(13, 10, 6, 2499.00, 14994.00),
(13, 11, 1, 2599.00, 2599.00),
-- 订单14：合肥人工智能公司
(14, 1, 10, 8999.00, 89990.00),
(14, 6, 1, 9999.00, 9999.00),
-- 订单15：济南云计算公司
(15, 5, 8, 3299.00, 26392.00),
(15, 10, 2, 2499.00, 4998.00);

-- 插入对话历史数据
INSERT INTO conversation_history (conversation_id, user_id, role, content, intent) VALUES
('conv-001', 1, 'user', '本月销售情况如何？', 'sales_query'),
('conv-001', 1, 'assistant', '根据查询结果，本月总销售额为¥285,967.00，共完成8笔订单。', 'sales_query'),
('conv-002', 2, 'user', '帮我查一下热销商品', 'product_query'),
('conv-002', 2, 'assistant', '热销商品排名：1. iPhone 15 Pro (25台) 2. MacBook Pro 14 (11台) 3. AirPods Pro 2 (30台)', 'product_query'),
('conv-003', 3, 'user', '张三的业绩怎么样？', 'employee_query'),
('conv-003', 3, 'assistant', '张三本月完成订单3笔，总销售额¥214,975.00，业绩排名第一。', 'employee_query');

-- 插入Markdown文档数据（用于RAG）
INSERT INTO markdown_document (title, file_name, content, upload_user_id) VALUES
('销售管理制度', 'sales_policy.md', '# 销售管理制度\n\n## 一、总则\n\n为规范公司销售行为，提高销售效率，特制定本制度。\n\n## 二、销售目标\n\n1. 每月销售目标由销售经理制定\n2. 销售人员需完成月度目标的80%以上\n3. 超额完成目标给予额外奖励\n\n## 三、客户管理\n\n1. 客户信息需及时录入系统\n2. 重要客户需定期回访\n3. 客户投诉需在24小时内响应', 1),
('产品知识手册', 'product_manual.md', '# 产品知识手册\n\n## 手机类\n\n### iPhone 15 Pro\n- 芯片：A17 Pro\n- 屏幕：6.1英寸 Super Retina XDR\n- 摄像头：48MP主摄 + 12MP超广角 + 12MP长焦\n- 价格：¥8,999\n\n### Samsung Galaxy S24 Ultra\n- 芯片：骁龙8 Gen3\n- 屏幕：6.8英寸 Dynamic AMOLED 2X\n- 摄像头：2亿像素主摄\n- 价格：¥9,999\n\n## 笔记本电脑类\n\n### MacBook Pro 14\n- 芯片：M3 Pro\n- 内存：18GB\n- 存储：512GB SSD\n- 价格：¥14,999', 1),
('售后服务流程', 'after_sales.md', '# 售后服务流程\n\n## 一、退换货政策\n\n1. 7天无理由退换货\n2. 15天质量问题换货\n3. 1年质保\n\n## 二、维修流程\n\n1. 客户提交维修申请\n2. 技术人员检测\n3. 确认维修方案\n4. 维修完成通知客户\n\n## 三、投诉处理\n\n1. 接收投诉\n2. 分析原因\n3. 制定解决方案\n4. 反馈客户\n5. 归档记录', 1);

-- 更新商品库存（根据已完成订单扣减）
UPDATE product p
SET stock_quantity = stock_quantity - (
    SELECT COALESCE(SUM(oi.quantity), 0)
    FROM order_item oi
    JOIN orders o ON oi.order_id = o.id
    WHERE oi.product_id = p.id AND o.status = 'COMPLETED'
);

-- 插入系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('company_name', '企业销售管理系统', '公司名称'),
('system_version', '1.0.0', '系统版本'),
('support_email', 'support@company.com', '技术支持邮箱'),
('max_upload_size', '10485760', '最大上传文件大小(字节)'),
('session_timeout', '3600', '会话超时时间(秒)');

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 完成
SELECT '测试数据初始化完成' AS message;
SELECT COUNT(*) AS user_count FROM sys_user;
SELECT COUNT(*) AS product_count FROM product;
SELECT COUNT(*) AS order_count FROM orders;
SELECT COUNT(*) AS order_item_count FROM order_item;
