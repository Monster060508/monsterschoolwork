package com.enterprise.sales.controller;

import com.enterprise.sales.entity.Product;
import com.enterprise.sales.service.ProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        // 获取商品列表
        List<Product> products = productService.getProducts(page, size, keyword);
        long total = productService.getProductCount(keyword);
        
        // 构建分页响应
        Map<String, Object> data = new HashMap<>();
        data.put("records", products);
        data.put("total", total);
        data.put("size", size);
        data.put("current", page);
        data.put("pages", (total + size - 1) / size);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", data));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.status(404).body(createResponse(404, "商品不存在", null));
        }
        
        return ResponseEntity.ok(createResponse(200, "获取成功", product));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.ok(createResponse(200, "创建成功", createdProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(createResponse(200, "更新成功", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            boolean success = productService.deleteProduct(id);
            if (success) {
                return ResponseEntity.ok(createResponse(200, "删除成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "删除失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PostMapping("/{id}/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestBody UploadImageRequest request) {
        try {
            String imageUrl = productService.uploadImage(id, request.getImageUrl());
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", imageUrl);
            
            return ResponseEntity.ok(createResponse(200, "上传成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchByKeyword(keyword);
        
        return ResponseEntity.ok(createResponse(200, "搜索成功", products));
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        List<Product> products = productService.findLowStockProducts(threshold);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", products));
    }
    
    @GetMapping("/hot")
    public ResponseEntity<?> getHotProducts(@RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.findHotProducts(limit);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", products));
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<?> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        
        List<Product> products = productService.findByPriceRange(minPrice, maxPrice);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", products));
    }
    
    @PostMapping("/{id}/update-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody UpdateStockRequest request) {
        try {
            boolean success = productService.updateStock(id, request.getQuantity());
            if (success) {
                return ResponseEntity.ok(createResponse(200, "库存更新成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "库存更新失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
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
    
    @Data
    public static class UploadImageRequest {
        private String imageUrl;
    }
    
    @Data
    public static class UpdateStockRequest {
        private Integer quantity;
    }
}