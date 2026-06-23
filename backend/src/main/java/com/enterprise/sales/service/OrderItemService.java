package com.enterprise.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.sales.entity.OrderItem;

import java.util.List;
import java.util.Map;

public interface OrderItemService extends IService<OrderItem> {
    
    /**
     * 根据订单ID查找订单项
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * 根据商品ID查找订单项
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * 获取订单项详情（包含商品信息）
     */
    List<Map<String, Object>> findOrderItemDetails(Long orderId);
    
    /**
     * 创建订单项
     */
    OrderItem createOrderItem(OrderItem orderItem);
    
    /**
     * 批量创建订单项
     */
    List<OrderItem> createOrderItems(List<OrderItem> orderItems);
    
    /**
     * 更新订单项
     */
    OrderItem updateOrderItem(Long id, OrderItem orderItem);
    
    /**
     * 删除订单项
     */
    boolean deleteOrderItem(Long id);
    
    /**
     * 根据订单ID删除所有订单项
     */
    boolean deleteByOrderId(Long orderId);
    
    /**
     * 统计商品的销售数量
     */
    List<Map<String, Object>> countByProduct();
    
    /**
     * 获取热销商品TOP N
     */
    List<Map<String, Object>> getHotProducts(int limit);
    
    /**
     * 计算订单总金额
     */
    java.math.BigDecimal calculateOrderTotal(Long orderId);
}