package com.enterprise.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.sales.entity.Order;
import com.enterprise.sales.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     */
    Order createOrder(Order order, List<Map<String, Object>> items);
    
    /**
     * 更新订单信息
     */
    Order updateOrder(Long id, Order order);
    
    /**
     * 删除订单（逻辑删除）
     */
    boolean deleteOrder(Long id);
    
    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long id, OrderStatus status);
    
    /**
     * 根据订单号查找订单
     */
    Order findByOrderNo(String orderNo);
    
    /**
     * 根据销售人员ID查找订单
     */
    List<Order> findBySalespersonId(Long salespersonId);
    
    /**
     * 根据状态查找订单
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * 根据时间范围查找订单
     */
    List<Order> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取订单列表（支持分页和筛选）
     */
    List<Order> getOrders(int page, int size, OrderStatus status, Long salespersonId, String keyword);
    
    /**
     * 获取订单总数
     */
    long getOrderCount(OrderStatus status, Long salespersonId, String keyword);
    
    /**
     * 统计各状态订单数量
     */
    Map<String, Long> countByStatus();
    
    /**
     * 获取销售排行榜
     */
    List<Map<String, Object>> getSalesRanking(int limit);
    
    /**
     * 获取订单趋势数据
     */
    List<Map<String, Object>> getOrderTrend(int days);
    
    /**
     * 获取总销售额
     */
    double getTotalSales();
    
    /**
     * 获取月度销售额
     */
    double getMonthlySales();
    
    /**
     * 获取日均销售额
     */
    double getDailyAverageSales();
    
    /**
     * 生成订单PDF
     */
    byte[] generateOrderPdf(Long orderId);
    
    /**
     * 完成订单（扣减库存）
     */
    boolean completeOrder(Long orderId);
    
    /**
     * 取消订单（回滚库存）
     */
    boolean cancelOrder(Long orderId, String reason);
    
    /**
     * 生成订单号
     */
    String generateOrderNo();
}