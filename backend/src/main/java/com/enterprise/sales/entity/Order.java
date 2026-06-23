package com.enterprise.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.sales.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("orders")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("order_no")
    private String orderNo;
    
    @TableField("customer_name")
    private String customerName;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("status")
    private OrderStatus status;
    
    @TableField("salesperson_id")
    private Long salespersonId;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    
    // 关联查询销售人员信息
    @TableField(exist = false)
    private User salesperson;
}