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
    
    @TableField("subtotal")
    private BigDecimal subtotal;
    
    // 关联查询商品信息
    @TableField(exist = false)
    private Product product;
    
    /**
     * 自动计算小计金额
     */
    public void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}