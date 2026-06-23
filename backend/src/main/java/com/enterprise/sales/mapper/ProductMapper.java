package com.enterprise.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.sales.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据关键词搜索商品
     */
    @Select("SELECT * FROM product WHERE (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) AND deleted = 0")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 查找库存不足的商品
     */
    @Select("SELECT * FROM product WHERE stock_quantity < #{threshold} AND deleted = 0")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
    
    /**
     * 查找热门商品（按销量排序）
     */
    @Select("SELECT p.*, COALESCE(SUM(oi.quantity), 0) as sales_quantity FROM product p LEFT JOIN order_item oi ON p.id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'COMPLETED' WHERE p.deleted = 0 GROUP BY p.id ORDER BY sales_quantity DESC LIMIT #{limit}")
    List<Product> findHotProducts(@Param("limit") int limit);
    
    /**
     * 根据价格区间查找商品
     */
    @Select("SELECT * FROM product WHERE price BETWEEN #{minPrice} AND #{maxPrice} AND deleted = 0")
    List<Product> findByPriceRange(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
    
    /**
     * 统计商品销量
     */
    @Select("SELECT p.id, p.name, COALESCE(SUM(oi.quantity), 0) as sales_quantity, COALESCE(SUM(oi.quantity * oi.unit_price), 0) as sales_amount FROM product p LEFT JOIN order_item oi ON p.id = oi.product_id LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'COMPLETED' WHERE p.deleted = 0 GROUP BY p.id, p.name ORDER BY sales_quantity DESC")
    List<Map<String, Object>> countByProduct();
}