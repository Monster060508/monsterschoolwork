package com.enterprise.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_item")
public class OrderItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("order_id")
    private Long orderId;
    
    @TableField("product_id")
    private Long productId;
    
    @TableField("quantity")
    private Integer quantity;
    
    @TableField("unit_price")
    private BigDecimal unitPrice;
    
    // 关联查询商品信息
    @TableField(exist = false)
    private Product product;
}