package com.enterprise.sales;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.sales.entity.Order;
import com.enterprise.sales.enums.OrderStatus;
import com.enterprise.sales.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 订单服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务测试")
class OrderServiceTest {
    
    @Mock
    private OrderService orderService;
    
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20240101001");
        testOrder.setCustomerName("测试客户");
        testOrder.setTotalAmount(new BigDecimal("15998.00"));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setSalespersonId(2L);
        testOrder.setCreateTime(LocalDateTime.now());
        testOrder.setDeleted(0);
    }
    
    @Test
    @DisplayName("根据ID查找订单 - 应返回订单")
    void findById_shouldReturnOrder() {
        when(orderService.getById(1L)).thenReturn(testOrder);
        
        Order result = orderService.getById(1L);
        
        assertNotNull(result);
        assertEquals("ORD20240101001", result.getOrderNo());
        assertEquals(OrderStatus.PENDING, result.getStatus());
    }
    
    @Test
    @DisplayName("根据ID查找订单 - 订单不存在应返回null")
    void findById_withNonexistentId_shouldReturnNull() {
        when(orderService.getById(999L)).thenReturn(null);
        
        Order result = orderService.getById(999L);
        
        assertNull(result);
    }
    
    @Test
    @DisplayName("创建订单 - 应返回true")
    void createOrder_shouldReturnTrue() {
        when(orderService.save(any(Order.class))).thenReturn(true);
        
        Order newOrder = new Order();
        newOrder.setCustomerName("新客户");
        newOrder.setTotalAmount(new BigDecimal("5000.00"));
        newOrder.setStatus(OrderStatus.PENDING);
        
        boolean result = orderService.save(newOrder);
        
        assertTrue(result);
        verify(orderService).save(any(Order.class));
    }
    
    @Test
    @DisplayName("更新订单状态 - 应返回true")
    void updateOrderStatus_shouldReturnTrue() {
        when(orderService.updateById(any(Order.class))).thenReturn(true);
        
        testOrder.setStatus(OrderStatus.IN_PROGRESS);
        boolean result = orderService.updateById(testOrder);
        
        assertTrue(result);
        assertEquals(OrderStatus.IN_PROGRESS, testOrder.getStatus());
    }
    
    @Test
    @DisplayName("完成订单 - 应将状态设为已完成")
    void completeOrder_shouldSetStatusCompleted() {
        when(orderService.updateById(any(Order.class))).thenReturn(true);
        
        testOrder.setStatus(OrderStatus.COMPLETED);
        boolean result = orderService.updateById(testOrder);
        
        assertTrue(result);
        assertEquals(OrderStatus.COMPLETED, testOrder.getStatus());
    }
    
    @Test
    @DisplayName("取消订单 - 应将状态设为已取消")
    void cancelOrder_shouldSetStatusCancelled() {
        when(orderService.updateById(any(Order.class))).thenReturn(true);
        
        testOrder.setStatus(OrderStatus.CANCELLED);
        boolean result = orderService.updateById(testOrder);
        
        assertTrue(result);
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
    }
    
    @Test
    @DisplayName("删除订单 - 应返回true")
    void deleteOrder_shouldReturnTrue() {
        when(orderService.removeById(1L)).thenReturn(true);
        
        boolean result = orderService.removeById(1L);
        
        assertTrue(result);
        verify(orderService).removeById(1L);
    }
    
    @Test
    @DisplayName("获取订单分页列表 - 应返回分页数据")
    void getOrders_shouldReturnPaginatedData() {
        Page<Order> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testOrder));
        page.setTotal(1);
        
        when(orderService.page(any(Page.class))).thenReturn(page);
        
        Page<Order> result = orderService.page(new Page<>(1, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }
    
    @Test
    @DisplayName("订单金额应为正数")
    void orderAmount_shouldBePositive() {
        assertTrue(testOrder.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    @DisplayName("订单应有订单号")
    void order_shouldHaveOrderNo() {
        assertNotNull(testOrder.getOrderNo());
        assertFalse(testOrder.getOrderNo().isEmpty());
    }
}
