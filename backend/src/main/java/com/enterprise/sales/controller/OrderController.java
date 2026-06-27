package com.enterprise.sales.controller;

import com.enterprise.sales.entity.Order;
import com.enterprise.sales.entity.OrderItem;
import com.enterprise.sales.entity.Product;
import com.enterprise.sales.entity.User;
import com.enterprise.sales.enums.OrderStatus;
import com.enterprise.sales.service.OrderService;
import com.enterprise.sales.service.OrderItemService;
import com.enterprise.sales.service.ProductService;
import com.enterprise.sales.service.UserService;
import com.enterprise.sales.service.PDFService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final UserService userService;
    private final PDFService pdfService;
    
    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long salespersonId,
            @RequestParam(required = false) String keyword) {
        
        // 获取订单列表
        List<Order> orders = orderService.getOrders(page, size, status, salespersonId, keyword);
        long total = orderService.getOrderCount(status, salespersonId, keyword);
        
        // 构建分页响应
        Map<String, Object> data = new HashMap<>();
        data.put("records", orders);
        data.put("total", total);
        data.put("size", size);
        data.put("current", page);
        data.put("pages", (total + size - 1) / size);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", data));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return ResponseEntity.status(404).body(createResponse(404, "订单不存在", null));
        }
        
        // 填充销售人员名称
        if (order.getSalespersonId() != null) {
            try {
                User salesperson = userService.getById(order.getSalespersonId());
                if (salesperson != null) {
                    order.setSalespersonName(salesperson.getName());
                }
            } catch (Exception e) {
                log.warn("查询销售人员失败: {}", e.getMessage());
            }
        }
        
        // 获取订单项信息并填充商品名称
        List<OrderItem> items = orderItemService.findByOrderId(id);
        if (items != null && !items.isEmpty()) {
            StringBuilder productNames = new StringBuilder();
            for (OrderItem item : items) {
                Product product = productService.getById(item.getProductId());
                if (product != null) {
                    if (productNames.length() > 0) {
                        productNames.append(", ");
                    }
                    productNames.append(product.getName());
                }
            }
            order.setProductName(productNames.toString());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", result));
    }
    
    @GetMapping("/{id}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long id) {
        List<OrderItem> items = orderItemService.findByOrderId(id);
        return ResponseEntity.ok(createResponse(200, "获取成功", items));
    }
    
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = new Order();
            order.setCustomerName(request.getCustomerName());
            order.setSalespersonId(request.getSalespersonId());
            
            Order createdOrder = orderService.createOrder(order, request.getItems());
            
            return ResponseEntity.ok(createResponse(200, "创建成功", createdOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody UpdateOrderRequest request) {
        try {
            Order order = new Order();
            order.setCustomerName(request.getCustomerName());
            order.setSalespersonId(request.getSalespersonId());
            order.setStatus(request.getStatus());
            
            Order updatedOrder = orderService.updateOrder(id, order);
            
            // 如果包含商品信息，更新订单项
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                // 删除旧的订单项
                orderItemService.deleteByOrderId(id);
                
                // 创建新的订单项
                List<OrderItem> newItems = new java.util.ArrayList<>();
                for (Map<String, Object> item : request.getItems()) {
                    Long productId = Long.valueOf(item.get("productId").toString());
                    Integer quantity = Integer.valueOf(item.get("quantity").toString());
                    
                    // 获取商品信息以设置单价
                    Product product = productService.getById(productId);
                    if (product == null) {
                        throw new RuntimeException("商品不存在: " + productId);
                    }
                    
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(id);
                    orderItem.setProductId(productId);
                    orderItem.setQuantity(quantity);
                    orderItem.setUnitPrice(product.getPrice());
                    newItems.add(orderItem);
                }
                orderItemService.createOrderItems(newItems);
                
                // 重新计算订单总金额
                java.math.BigDecimal totalAmount = orderItemService.calculateOrderTotal(id);
                // 更新订单总金额
                Order orderToUpdate = orderService.getById(id);
                if (orderToUpdate != null) {
                    orderToUpdate.setTotalAmount(totalAmount);
                    orderService.updateById(orderToUpdate);
                }
            }
            
            return ResponseEntity.ok(createResponse(200, "更新成功", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.deleteOrder(id);
            if (success) {
                return ResponseEntity.ok(createResponse(200, "删除成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "删除失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        try {
            boolean success = orderService.updateOrderStatus(id, request.getStatus());
            if (success) {
                return ResponseEntity.ok(createResponse(200, "状态更新成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "状态更新失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportOrderPdf(@PathVariable Long id) {
        try {
            Order order = orderService.getById(id);
            if (order == null) {
                return ResponseEntity.badRequest().body(null);
            }
            
            byte[] pdfBytes = pdfService.generateOrderPdf(order);
            
            HttpHeaders headers = new HttpHeaders();
            // 设置PDF MIME类型
            headers.setContentType(MediaType.APPLICATION_PDF);
            // 使用URLEncoder编码文件名，支持中文
            String fileName = java.net.URLEncoder.encode("订单_" + id + ".pdf", "UTF-8").replace("+", "%20");
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("导出订单PDF失败, orderId={}", id, e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            boolean success = orderService.completeOrder(id);
            if (success) {
                return ResponseEntity.ok(createResponse(200, "订单完成", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "订单完成失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, @RequestBody CancelOrderRequest request) {
        try {
            boolean success = orderService.cancelOrder(id, request.getReason());
            if (success) {
                return ResponseEntity.ok(createResponse(200, "订单取消成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "订单取消失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @GetMapping("/status-count")
    public ResponseEntity<?> getStatusCount() {
        Map<String, Long> statusCount = orderService.countByStatus();
        
        return ResponseEntity.ok(createResponse(200, "获取成功", statusCount));
    }
    
    private Map<String, Object> createResponse(int code, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    @Data
    public static class CreateOrderRequest {
        private String customerName;
        private Long salespersonId;
        private List<Map<String, Object>> items;
    }
    
    @Data
    public static class UpdateOrderRequest {
        private String customerName;
        private Long salespersonId;
        private OrderStatus status;
        private List<Map<String, Object>> items;
    }
    
    @Data
    public static class UpdateStatusRequest {
        private OrderStatus status;
    }
    
    @Data
    public static class CancelOrderRequest {
        private String reason;
    }
}