package com.enterprise.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.sales.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    /**
     * 根据订单ID查找订单项
     */
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据商品ID查找订单项
     */
    @Select("SELECT * FROM order_item WHERE product_id = #{productId}")
    List<OrderItem> findByProductId(@Param("productId") Long productId);
    
    /**
     * 获取订单项详情（包含商品信息）
     */
    @Select("SELECT oi.*, p.name as product_name, p.image_url as product_image FROM order_item oi LEFT JOIN product p ON oi.product_id = p.id WHERE oi.order_id = #{orderId}")
    List<Map<String, Object>> findOrderItemDetails(@Param("orderId") Long orderId);
    
    /**
     * 统计商品的销售数量
     */
    @Select("SELECT product_id, SUM(quantity) as total_quantity FROM order_item GROUP BY product_id")
    List<Map<String, Object>> countByProduct();
    
    /**
     * 获取热销商品TOP N
     */
    @Select("SELECT oi.product_id, p.name as product_name, p.image_url, SUM(oi.quantity) as sales_quantity, SUM(oi.quantity * oi.unit_price) as sales_amount FROM order_item oi LEFT JOIN product p ON oi.product_id = p.id LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'COMPLETED' GROUP BY oi.product_id, p.name, p.image_url ORDER BY sales_quantity DESC LIMIT #{limit}")
    List<Map<String, Object>> getHotProducts(@Param("limit") int limit);
}