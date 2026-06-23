package com.enterprise.sales.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    ADMIN("ADMIN", "管理员"),
    SALES("SALES", "销售");
    
    @EnumValue
    private final String code;
    private final String description;
    
    UserRole(String code, String description) {
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
    
    @JsonCreator
    public static UserRole fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("用户角色不能为空");
        }
        // 先按code匹配
        for (UserRole role : values()) {
            if (role.code.equalsIgnoreCase(value)) {
                return role;
            }
        }
        // 再按description匹配
        for (UserRole role : values()) {
            if (role.description.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的用户角色: " + value);
    }
    
    public static UserRole fromCode(String code) {
        return fromValue(code);
    }
}