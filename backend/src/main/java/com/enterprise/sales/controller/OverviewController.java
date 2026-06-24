package com.enterprise.sales.controller;

import com.enterprise.sales.service.OrderService;
import com.enterprise.sales.service.OrderItemService;
import com.enterprise.sales.service.PDFService;
import com.enterprise.sales.service.ProductService;
import com.enterprise.sales.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/overview")
@RequiredArgsConstructor
public class OverviewController {
    
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final PDFService pdfService;
    private final ProductService productService;
    private final UserService userService;
    
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        // 获取销售统计数据
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalSales", orderService.getTotalSales());
        statistics.put("monthlySales", orderService.getMonthlySales());
        statistics.put("dailySales", orderService.getDailyAverageSales());
        
        // 获取订单状态统计
        Map<String, Long> statusCount = orderService.countByStatus();
        statistics.put("totalOrders", statusCount.values().stream().mapToLong(Long::longValue).sum());
        statistics.put("pendingOrders", statusCount.getOrDefault("PENDING", 0L));
        statistics.put("inProgressOrders", statusCount.getOrDefault("IN_PROGRESS", 0L));
        statistics.put("completedOrders", statusCount.getOrDefault("COMPLETED", 0L));
        statistics.put("cancelledOrders", statusCount.getOrDefault("CANCELLED", 0L));
        
        // 获取商品和客户数量
        statistics.put("totalProducts", productService.count());
        statistics.put("totalCustomers", userService.count());
        
        return ResponseEntity.ok(createResponse(200, "获取成功", statistics));
    }
    
    @GetMapping("/sales-ranking")
    public ResponseEntity<?> getSalesRanking(@RequestParam(defaultValue = "10") int limit) {
        // 获取销售排行榜
        List<Map<String, Object>> salesRanking = orderService.getSalesRanking(limit);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", salesRanking));
    }
    
    @GetMapping("/hot-products")
    public ResponseEntity<?> getHotProducts(@RequestParam(defaultValue = "10") int limit) {
        // 获取热销商品
        List<Map<String, Object>> hotProducts = orderItemService.getHotProducts(limit);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", hotProducts));
    }
    
    @GetMapping("/order-trend")
    public ResponseEntity<?> getOrderTrend(@RequestParam(defaultValue = "30") int days) {
        // 获取订单趋势数据
        List<Map<String, Object>> orderTrend = orderService.getOrderTrend(days);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", orderTrend));
    }
    
    @GetMapping("/recent-orders")
    public ResponseEntity<?> getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        // 获取最近订单
        List<Map<String, Object>> recentOrders = orderService.getRecentOrders(limit);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", recentOrders));
    }
    
    @GetMapping("/export-pdf")
    public ResponseEntity<byte[]> exportOverviewPdf() {
        try {
            // 获取统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalSales", orderService.getTotalSales());
            statistics.put("monthlySales", orderService.getMonthlySales());
            statistics.put("dailySales", orderService.getDailyAverageSales());
            
            Map<String, Long> statusCount = orderService.countByStatus();
            statistics.put("totalOrders", statusCount.values().stream().mapToLong(Long::longValue).sum());
            
            // 获取销售排行榜
            List<Map<String, Object>> salesRanking = orderService.getSalesRanking(10);
            
            // 获取热销商品
            List<Map<String, Object>> hotProducts = orderItemService.getHotProducts(10);
            
            // 生成PDF
            byte[] pdfBytes = pdfService.generateOverviewPdf(statistics, salesRanking, hotProducts);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "总览报表.docx");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    private Map<String, Object> createResponse(int code, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}