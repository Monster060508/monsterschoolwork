-- 企业销售管理系统初始数据脚本
USE sales_management;

-- 插入管理员用户（密码：admin123，BCrypt加密）
INSERT INTO sys_user (username, password, role, name, photo_url) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', '系统管理员', 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/user/admin.jpg');

-- 插入销售用户（密码：sales123，BCrypt加密）
INSERT INTO sys_user (username, password, role, name, photo_url) VALUES
('sales1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SALESPERSON', '张三', 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/user/sales1.jpg'),
('sales2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SALESPERSON', '李四', 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/user/sales2.jpg'),
('sales3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'SALESPERSON', '王五', 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/user/sales3.jpg');

-- 插入商品数据
INSERT INTO product (name, description, price, stock_quantity, image_url) VALUES
('iPhone 15', '苹果最新款智能手机，A17芯片，4800万像素摄像头', 7999.00, 100, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/iphone15.jpg'),
('iPhone 15 Pro', '苹果专业版智能手机，钛金属设计，5倍光学变焦', 8999.00, 80, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/iphone15pro.jpg'),
('MacBook Pro 14', '苹果专业级笔记本电脑，M3 Pro芯片，18小时续航', 14999.00, 50, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/macbookpro14.jpg'),
('MacBook Air M2', '苹果轻薄笔记本电脑，M2芯片，无风扇设计', 9999.00, 60, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/macbookair.jpg'),
('iPad Pro 12.9', '苹果专业级平板电脑，M2芯片，mini-LED显示屏', 8999.00, 40, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/ipadpro.jpg'),
('AirPods Pro 2', '苹果降噪耳机，自适应透明模式，个性化空间音频', 1899.00, 200, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/airpodspro.jpg'),
('Apple Watch Series 9', '苹果智能手表，S9芯片，双指互点手势', 2999.00, 120, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/applewatch.jpg'),
('Samsung Galaxy S24', '三星旗舰智能手机，AI功能，2亿像素摄像头', 6999.00, 90, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/galaxys24.jpg'),
('Dell XPS 15', '戴尔高端笔记本电脑，4K OLED显示屏，RTX 4060显卡', 12999.00, 30, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/dellxps15.jpg'),
('Sony WH-1000XM5', '索尼降噪耳机，30小时续航，LDAC编码', 2499.00, 150, 'https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/sonyxm5.jpg');

-- 插入订单数据
INSERT INTO orders (order_no, customer_name, total_amount, status, salesperson_id) VALUES
('ORD20240101001', '北京科技有限公司', 15998.00, 'COMPLETED', 2),
('ORD20240101002', '上海创新科技有限公司', 8999.00, 'IN_PROGRESS', 3),
('ORD20240101003', '深圳智能科技有限公司', 23997.00, 'PENDING', 2),
('ORD20240101004', '广州数字科技有限公司', 14999.00, 'COMPLETED', 4),
('ORD20240101005', '杭州互联网科技有限公司', 9999.00, 'CANCELLED', 3),
('ORD20240102001', '成都软件科技有限公司', 18990.00, 'COMPLETED', 2),
('ORD20240102002', '武汉光谷科技有限公司', 8999.00, 'IN_PROGRESS', 4),
('ORD20240102003', '西安高新技术有限公司', 29990.00, 'PENDING', 3),
('ORD20240102004', '南京信息技术有限公司', 12999.00, 'COMPLETED', 2),
('ORD20240102005', '天津经济技术有限公司', 6999.00, 'CANCELLED', 4);

-- 插入订单项数据
-- 订单1：iPhone 15 x2
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 2, 7999.00);

-- 订单2：iPhone 15 Pro x1
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(2, 2, 1, 8999.00);

-- 订单3：MacBook Pro 14 x1, AirPods Pro 2 x2
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(3, 3, 1, 14999.00),
(3, 6, 2, 1899.00);

-- 订单4：MacBook Air M2 x1, iPad Pro 12.9 x1
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(4, 4, 1, 9999.00),
(4, 5, 1, 8999.00);

-- 订单5：Dell XPS 15 x1
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(5, 9, 1, 12999.00);

-- 订单6：iPhone 15 x1, MacBook Pro 14 x1
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(6, 1, 1, 7999.00),
(6, 3, 1, 14999.00);

-- 订单7：Samsung Galaxy S24 x1, Sony WH-1000XM5 x1
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(7, 8, 1, 6999.00),
(7, 10, 1, 2499.00);

-- 订单8：MacBook Pro 14 x2, iPad Pro 12.9 x1
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(8, 3, 2, 14999.00),
(8, 5, 1, 8999.00);

-- 订单9：Dell XPS 15 x1, Apple Watch Series 9 x1
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(9, 9, 1, 12999.00),
(9, 7, 1, 2999.00);

-- 订单10：Samsung Galaxy S24 x1
INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(10, 8, 1, 6999.00);

-- 更新订单总金额（根据订单项计算）
UPDATE orders o
SET total_amount = (
    SELECT SUM(oi.quantity * oi.unit_price)
    FROM order_item oi
    WHERE oi.order_id = o.id
)
WHERE o.id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

-- 扣减已完成订单的库存
UPDATE product p
SET stock_quantity = stock_quantity - (
    SELECT COALESCE(SUM(oi.quantity), 0)
    FROM order_item oi
    JOIN orders o ON oi.order_id = o.id
    WHERE oi.product_id = p.id AND o.status = 'COMPLETED'
)
WHERE p.id IN (
    SELECT DISTINCT oi.product_id
    FROM order_item oi
    JOIN orders o ON oi.order_id = o.id
    WHERE o.status = 'COMPLETED'
);

-- 插入订单状态变更日志
INSERT INTO order_status_log (order_id, old_status, new_status, operator_id, remark) VALUES
(1, NULL, 'PENDING', 2, '创建订单'),
(1, 'PENDING', 'IN_PROGRESS', 2, '开始处理'),
(1, 'IN_PROGRESS', 'COMPLETED', 2, '订单完成'),
(2, NULL, 'PENDING', 3, '创建订单'),
(2, 'PENDING', 'IN_PROGRESS', 3, '开始处理'),
(3, NULL, 'PENDING', 2, '创建订单'),
(4, NULL, 'PENDING', 4, '创建订单'),
(4, 'PENDING', 'IN_PROGRESS', 4, '开始处理'),
(4, 'IN_PROGRESS', 'COMPLETED', 4, '订单完成'),
(5, NULL, 'PENDING', 3, '创建订单'),
(5, 'PENDING', 'CANCELLED', 3, '客户取消订单'),
(6, NULL, 'PENDING', 2, '创建订单'),
(6, 'PENDING', 'IN_PROGRESS', 2, '开始处理'),
(6, 'IN_PROGRESS', 'COMPLETED', 2, '订单完成'),
(7, NULL, 'PENDING', 4, '创建订单'),
(7, 'PENDING', 'IN_PROGRESS', 4, '开始处理'),
(8, NULL, 'PENDING', 3, '创建订单'),
(9, NULL, 'PENDING', 2, '创建订单'),
(9, 'PENDING', 'IN_PROGRESS', 2, '开始处理'),
(9, 'IN_PROGRESS', 'COMPLETED', 2, '订单完成'),
(10, NULL, 'PENDING', 4, '创建订单'),
(10, 'PENDING', 'CANCELLED', 4, '库存不足取消');

-- 插入库存变更日志
INSERT INTO stock_change_log (product_id, change_type, change_quantity, before_quantity, after_quantity, order_id, operator_id, remark) VALUES
(1, 'ORDER_COMPLETE', -2, 100, 98, 1, 2, '订单完成扣减库存，订单号：ORD20240101001'),
(4, 'ORDER_COMPLETE', -1, 60, 59, 4, 4, '订单完成扣减库存，订单号：ORD20240101004'),
(5, 'ORDER_COMPLETE', -1, 40, 39, 4, 4, '订单完成扣减库存，订单号：ORD20240101004'),
(1, 'ORDER_COMPLETE', -1, 98, 97, 6, 2, '订单完成扣减库存，订单号：ORD20240102001'),
(3, 'ORDER_COMPLETE', -1, 50, 49, 6, 2, '订单完成扣减库存，订单号：ORD20240102001'),
(9, 'ORDER_COMPLETE', -1, 30, 29, 9, 2, '订单完成扣减库存，订单号：ORD20240102004'),
(7, 'ORDER_COMPLETE', -1, 120, 119, 9, 2, '订单完成扣减库存，订单号：ORD20240102004');

-- 插入销售统计数据
INSERT INTO sales_statistics (stat_date, total_sales, order_count, completed_order_count, cancelled_order_count) VALUES
('2024-01-01', 63994.00, 5, 2, 1),
('2024-01-02', 57987.00, 5, 2, 1);

-- 插入销售员日报表
INSERT INTO salesperson_daily_report (salesperson_id, report_date, daily_sales, order_count, completed_order_count) VALUES
(2, '2024-01-01', 15998.00, 2, 1),
(3, '2024-01-01', 8999.00, 1, 0),
(4, '2024-01-01', 14999.00, 1, 1),
(2, '2024-01-02', 18990.00, 2, 2),
(3, '2024-01-02', 29990.00, 1, 0),
(4, '2024-01-02', 8999.00, 1, 0);

-- 插入商品销售统计数据
INSERT INTO product_sales_statistics (product_id, stat_date, sales_quantity, sales_amount) VALUES
(1, '2024-01-01', 2, 15998.00),
(2, '2024-01-01', 1, 8999.00),
(3, '2024-01-01', 1, 14999.00),
(4, '2024-01-01', 1, 9999.00),
(6, '2024-01-01', 2, 3798.00),
(9, '2024-01-01', 1, 12999.00),
(1, '2024-01-02', 1, 7999.00),
(3, '2024-01-02', 1, 14999.00),
(5, '2024-01-02', 1, 8999.00),
(7, '2024-01-02', 1, 2999.00),
(8, '2024-01-02', 1, 6999.00),
(9, '2024-01-02', 1, 12999.00),
(10, '2024-01-02', 1, 2499.00);

-- 插入系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('site.name', '企业销售管理系统', '网站名称'),
('site.description', '基于Spring Boot + Vue的企业销售管理系统', '网站描述'),
('upload.maxSize', '10485760', '上传文件最大大小（字节）'),
('upload.allowedTypes', 'jpg,jpeg,png,gif,webp', '允许上传的文件类型'),
('order.autoCancelDays', '7', '订单自动取消天数'),
('stock.warningThreshold', '10', '库存预警阈值'),
('report.exportPath', '/tmp/reports', '报表导出路径');

-- 插入数据字典
INSERT INTO data_dictionary (dict_type, dict_code, dict_value, sort_order) VALUES
-- 订单状态
('order_status', 'PENDING', '待处理', 1),
('order_status', 'IN_PROGRESS', '进行中', 2),
('order_status', 'COMPLETED', '已完成', 3),
('order_status', 'CANCELLED', '已取消', 4),
-- 用户角色
('user_role', 'ADMIN', '管理员', 1),
('user_role', 'SALESPERSON', '销售', 2),
-- 文件类型
('file_type', 'image', '图片', 1),
('file_type', 'document', '文档', 2),
('file_type', 'video', '视频', 3),
-- 操作类型
('operation_type', 'CREATE', '创建', 1),
('operation_type', 'UPDATE', '更新', 2),
('operation_type', 'DELETE', '删除', 3),
('operation_type', 'QUERY', '查询', 4),
('operation_type', 'EXPORT', '导出', 5);

-- 插入向量文档（示例）
INSERT INTO vector_document (title, content, metadata) VALUES
('销售管理系统使用指南', '本系统是一个企业级销售管理系统，包含总览、商品管理、订单管理、员工管理和智能问答五大模块。总览模块提供销售数据可视化，商品管理模块支持商品信息维护和图片上传，订单管理模块处理订单全生命周期，员工管理模块负责用户权限管理，智能问答模块提供AI驱动的数据分析。', '{"category": "documentation", "type": "guide"}'),
('常见问题解答', 'Q: 如何重置密码？A: 管理员可以在员工管理页面重置用户密码。Q: 如何导出订单PDF？A: 在订单详情页面点击导出PDF按钮即可。Q: 库存不足怎么办？A: 系统会自动预警低库存商品，管理员可以手动调整库存。', '{"category": "faq", "type": "help"}'),
('销售技巧分享', '成功的销售需要了解客户需求，建立信任关系，提供专业建议。重点推荐高利润商品，关注客户反馈，及时跟进订单状态。定期分析销售数据，优化销售策略。', '{"category": "knowledge", "type": "sales_tips"}');

-- 插入定时任务
INSERT INTO scheduled_task (task_name, task_group, cron_expression, invoke_target, status) VALUES
('每日统计任务', 'statistics', '0 0 1 * * ?', 'statisticsTask.updateDailyStatistics', 'RUNNING'),
('库存预警检查', 'inventory', '0 0 */2 * * ?', 'inventoryTask.checkStockWarning', 'RUNNING'),
('订单超时检查', 'order', '0 */30 * * * ?', 'orderTask.checkOrderTimeout', 'RUNNING'),
('数据备份任务', 'system', '0 0 2 * * ?', 'backupTask.backupDatabase', 'RUNNING');

-- 完成初始数据插入
SELECT '初始数据插入完成' AS message;