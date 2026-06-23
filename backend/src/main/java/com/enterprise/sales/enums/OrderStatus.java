package com.enterprise.sales.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING("pending", "待处理"),
    IN_PROGRESS("in_progress", "进行中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");
    
    @EnumValue
    private final String code;
    private final String description;
    
    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    @JsonValue
    public String getDescription() {
        return description;
    }
    
    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的订单状态: " + code);
    }
}