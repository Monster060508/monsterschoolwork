package com.enterprise.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.sales.entity.Product;
import com.enterprise.sales.mapper.ProductMapper;
import com.enterprise.sales.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    
    private final ProductMapper productMapper;
    
    @Override
    @Transactional
    public Product createProduct(Product product) {
        // 设置默认值
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setDeleted(0);
        
        // 保存商品
        productMapper.insert(product);
        
        return product;
    }
    
    @Override
    @Transactional
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = getById(id);
        if (existingProduct == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 更新商品信息
        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getStockQuantity() != null) {
            existingProduct.setStockQuantity(product.getStockQuantity());
        }
        if (product.getImageUrl() != null) {
            existingProduct.setImageUrl(product.getImageUrl());
        }
        
        existingProduct.setUpdateTime(LocalDateTime.now());
        
        // 保存更新
        updateById(existingProduct);
        
        return existingProduct;
    }
    
    @Override
    @Transactional
    public boolean deleteProduct(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 使用自定义SQL直接更新deleted字段，避免@TableLogic导致更新失败
        int updatedRows = productMapper.markProductDeleted(id);
        if (updatedRows == 0) {
            throw new RuntimeException("删除商品失败，数据库更新失败");
        }
        
        return true;
    }
    
    @Override
    public List<Product> searchByKeyword(String keyword) {
        return productMapper.searchByKeyword(keyword);
    }
    
    @Override
    public List<Product> findLowStockProducts(int threshold) {
        return productMapper.findLowStockProducts(threshold);
    }
    
    @Override
    public List<Product> findHotProducts(int limit) {
        return productMapper.findHotProducts(limit);
    }
    
    @Override
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productMapper.findByPriceRange(minPrice.doubleValue(), maxPrice.doubleValue());
    }
    
    @Override
    @Transactional
    public String uploadImage(Long productId, String imageUrl) {
        Product product = getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        product.setImageUrl(imageUrl);
        product.setUpdateTime(LocalDateTime.now());
        
        updateById(product);
        
        return imageUrl;
    }
    
    @Override
    @Transactional
    public boolean updateStock(Long productId, int quantity) {
        Product product = getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        product.setStockQuantity(quantity);
        product.setUpdateTime(LocalDateTime.now());
        
        return updateById(product);
    }
    
    @Override
    @Transactional
    public boolean deductStock(Long productId, int quantity) {
        Product product = getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 检查库存是否充足
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        // 扣减库存
        product.setStockQuantity(product.getStockQuantity() - quantity);
        product.setUpdateTime(LocalDateTime.now());
        
        return updateById(product);
    }
    
    @Override
    @Transactional
    public boolean addStock(Long productId, int quantity) {
        Product product = getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 增加库存
        product.setStockQuantity(product.getStockQuantity() + quantity);
        product.setUpdateTime(LocalDateTime.now());
        
        return updateById(product);
    }
    
    @Override
    public List<Product> getProducts(int page, int size, String keyword) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("name", keyword)
                .or()
                .like("description", keyword)
            );
        }
        
        queryWrapper.orderByDesc("create_time");
        
        // 分页
        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + offset + ", " + size);
        
        return list(queryWrapper);
    }
    
    @Override
    public long getProductCount(String keyword) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("name", keyword)
                .or()
                .like("description", keyword)
            );
        }
        
        return count(queryWrapper);
    }
    
    @Override
    public List<Map<String, Object>> getProductSalesStatistics() {
        return productMapper.countByProduct();
    }
    
    @Override
    public boolean checkStock(Long productId, int quantity) {
        Product product = getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        return product.getStockQuantity() >= quantity;
    }
}