package com.enterprise.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.sales.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {
    
    /**
     * 创建商品
     */
    Product createProduct(Product product);
    
    /**
     * 更新商品信息
     */
    Product updateProduct(Long id, Product product);
    
    /**
     * 删除商品（逻辑删除）
     */
    boolean deleteProduct(Long id);
    
    /**
     * 根据关键词搜索商品
     */
    List<Product> searchByKeyword(String keyword);
    
    /**
     * 查找库存不足的商品
     */
    List<Product> findLowStockProducts(int threshold);
    
    /**
     * 查找热门商品（按销量排序）
     */
    List<Product> findHotProducts(int limit);
    
    /**
     * 根据价格区间查找商品
     */
    List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * 上传商品图片
     */
    String uploadImage(Long productId, String imageUrl);
    
    /**
     * 更新商品库存
     */
    boolean updateStock(Long productId, int quantity);
    
    /**
     * 扣减库存
     */
    boolean deductStock(Long productId, int quantity);
    
    /**
     * 增加库存
     */
    boolean addStock(Long productId, int quantity);
    
    /**
     * 获取商品列表（支持分页和搜索）
     */
    List<Product> getProducts(int page, int size, String keyword);
    
    /**
     * 获取商品总数
     */
    long getProductCount(String keyword);
    
    /**
     * 获取商品销售统计
     */
    List<Map<String, Object>> getProductSalesStatistics();
    
    /**
     * 检查库存是否充足
     */
    boolean checkStock(Long productId, int quantity);
}