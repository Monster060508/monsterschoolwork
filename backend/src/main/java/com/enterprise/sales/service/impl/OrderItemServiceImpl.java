package com.enterprise.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.sales.entity.OrderItem;
import com.enterprise.sales.mapper.OrderItemMapper;
import com.enterprise.sales.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
    
    private final OrderItemMapper orderItemMapper;
    
    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemMapper.findByOrderId(orderId);
    }
    
    @Override
    public List<OrderItem> findByProductId(Long productId) {
        return orderItemMapper.findByProductId(productId);
    }
    
    @Override
    public List<Map<String, Object>> findOrderItemDetails(Long orderId) {
        return orderItemMapper.findOrderItemDetails(orderId);
    }
    
    @Override
    @Transactional
    public OrderItem createOrderItem(OrderItem orderItem) {
        // 自动计算小计金额
        orderItem.calculateSubtotal();
        // 保存订单项
        orderItemMapper.insert(orderItem);
        
        return orderItem;
    }
    
    @Override
    @Transactional
    public List<OrderItem> createOrderItems(List<OrderItem> orderItems) {
        // 批量保存订单项
        for (OrderItem orderItem : orderItems) {
            // 自动计算小计金额
            orderItem.calculateSubtotal();
            orderItemMapper.insert(orderItem);
        }
        
        return orderItems;
    }
    
    @Override
    @Transactional
    public OrderItem updateOrderItem(Long id, OrderItem orderItem) {
        OrderItem existingItem = getById(id);
        if (existingItem == null) {
            throw new RuntimeException("订单项不存在");
        }
        
        // 更新订单项信息
        if (orderItem.getQuantity() != null) {
            existingItem.setQuantity(orderItem.getQuantity());
        }
        if (orderItem.getUnitPrice() != null) {
            existingItem.setUnitPrice(orderItem.getUnitPrice());
        }
        
        // 保存更新
        updateById(existingItem);
        
        return existingItem;
    }
    
    @Override
    @Transactional
    public boolean deleteOrderItem(Long id) {
        OrderItem orderItem = getById(id);
        if (orderItem == null) {
            throw new RuntimeException("订单项不存在");
        }
        
        return removeById(id);
    }
    
    @Override
    @Transactional
    public boolean deleteByOrderId(Long orderId) {
        QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        
        return remove(queryWrapper);
    }
    
    @Override
    public List<Map<String, Object>> countByProduct() {
        return orderItemMapper.countByProduct();
    }
    
    @Override
    public List<Map<String, Object>> getHotProducts(int limit) {
        return orderItemMapper.getHotProducts(limit);
    }
    
    @Override
    public BigDecimal calculateOrderTotal(Long orderId) {
        List<OrderItem> orderItems = findByOrderId(orderId);
        
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem orderItem : orderItems) {
            BigDecimal subtotal = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            total = total.add(subtotal);
        }
        
        return total;
    }
}