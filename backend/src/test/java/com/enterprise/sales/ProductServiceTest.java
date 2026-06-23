package com.enterprise.sales;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.sales.entity.Product;
import com.enterprise.sales.service.ProductService;
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
 * 商品服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("商品服务测试")
class ProductServiceTest {
    
    @Mock
    private ProductService productService;
    
    private Product testProduct;
    
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("iPhone 15");
        testProduct.setDescription("苹果最新款手机");
        testProduct.setPrice(new BigDecimal("7999.00"));
        testProduct.setStockQuantity(100);
        testProduct.setImageUrl("https://example.com/iphone15.jpg");
        testProduct.setCreateTime(LocalDateTime.now());
        testProduct.setDeleted(0);
    }
    
    @Test
    @DisplayName("根据ID查找商品 - 应返回商品")
    void findById_shouldReturnProduct() {
        when(productService.getById(1L)).thenReturn(testProduct);
        
        Product result = productService.getById(1L);
        
        assertNotNull(result);
        assertEquals("iPhone 15", result.getName());
        assertEquals(new BigDecimal("7999.00"), result.getPrice());
    }
    
    @Test
    @DisplayName("根据ID查找商品 - 商品不存在应返回null")
    void findById_withNonexistentId_shouldReturnNull() {
        when(productService.getById(999L)).thenReturn(null);
        
        Product result = productService.getById(999L);
        
        assertNull(result);
    }
    
    @Test
    @DisplayName("创建商品 - 应返回true")
    void createProduct_shouldReturnTrue() {
        when(productService.save(any(Product.class))).thenReturn(true);
        
        Product newProduct = new Product();
        newProduct.setName("MacBook Pro");
        newProduct.setPrice(new BigDecimal("12999.00"));
        newProduct.setStockQuantity(50);
        
        boolean result = productService.save(newProduct);
        
        assertTrue(result);
        verify(productService).save(any(Product.class));
    }
    
    @Test
    @DisplayName("更新商品 - 应返回true")
    void updateProduct_shouldReturnTrue() {
        when(productService.updateById(any(Product.class))).thenReturn(true);
        
        testProduct.setPrice(new BigDecimal("6999.00"));
        boolean result = productService.updateById(testProduct);
        
        assertTrue(result);
        verify(productService).updateById(testProduct);
    }
    
    @Test
    @DisplayName("删除商品 - 应返回true")
    void deleteProduct_shouldReturnTrue() {
        when(productService.removeById(1L)).thenReturn(true);
        
        boolean result = productService.removeById(1L);
        
        assertTrue(result);
        verify(productService).removeById(1L);
    }
    
    @Test
    @DisplayName("获取商品分页列表 - 应返回分页数据")
    void getProducts_shouldReturnPaginatedData() {
        Page<Product> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testProduct));
        page.setTotal(1);
        
        when(productService.page(any(Page.class))).thenReturn(page);
        
        Page<Product> result = productService.page(new Page<>(1, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }
    
    @Test
    @DisplayName("商品价格应为正数")
    void productPrice_shouldBePositive() {
        assertTrue(testProduct.getPrice().compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    @DisplayName("商品库存应为非负数")
    void productStock_shouldBeNonNegative() {
        assertTrue(testProduct.getStockQuantity() >= 0);
    }
}
