package com.enterprise.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.sales.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 根据订单号查找订单
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo} AND deleted = 0")
    Order findByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 根据销售人员ID查找订单
     */
    @Select("SELECT * FROM orders WHERE salesperson_id = #{salespersonId} AND deleted = 0")
    List<Order> findBySalespersonId(@Param("salespersonId") Long salespersonId);
    
    /**
     * 根据状态查找订单
     */
    @Select("SELECT * FROM orders WHERE status = #{status} AND deleted = 0")
    List<Order> findByStatus(@Param("status") String status);
    
    /**
     * 根据时间范围查找订单
     */
    @Select("SELECT * FROM orders WHERE create_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0")
    List<Order> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计各状态订单数量
     */
    @Select("SELECT status, COUNT(*) as count FROM orders WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();
    
    /**
     * 统计销售人员的订单数量
     */
    @Select("SELECT salesperson_id, COUNT(*) as order_count, SUM(total_amount) as total_sales FROM orders WHERE status = 'COMPLETED' AND deleted = 0 GROUP BY salesperson_id")
    List<Map<String, Object>> countBySalesperson();
    
    /**
     * 获取销售排行榜
     */
    @Select("SELECT u.id as salesperson_id, u.name as salesperson_name, u.photo_url, COUNT(o.id) as order_count, COALESCE(SUM(o.total_amount), 0) as total_sales FROM sys_user u LEFT JOIN orders o ON u.id = o.salesperson_id AND o.status = 'COMPLETED' AND o.deleted = 0 WHERE u.role = 'SALESPERSON' AND u.deleted = 0 GROUP BY u.id, u.name, u.photo_url ORDER BY total_sales DESC LIMIT #{limit}")
    List<Map<String, Object>> getSalesRanking(@Param("limit") int limit);
    
    /**
     * 获取订单趋势数据
     */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as order_count, SUM(total_amount) as sales_amount FROM orders WHERE create_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> getOrderTrend(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取总销售额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'COMPLETED' AND deleted = 0")
    double getTotalSales();
    
    /**
     * 获取月度销售额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'COMPLETED' AND YEAR(create_time) = YEAR(CURRENT_DATE()) AND MONTH(create_time) = MONTH(CURRENT_DATE()) AND deleted = 0")
    double getMonthlySales();
    
    /**
     * 获取日均销售额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) / COUNT(DISTINCT DATE(create_time)) FROM orders WHERE status = 'COMPLETED' AND create_time >= DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY) AND deleted = 0")
    double getDailyAverageSales();
}