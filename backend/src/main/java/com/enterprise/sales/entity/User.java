package com.enterprise.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.sales.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    private String username;
    
    @TableField("password")
    private String password;
    
    @TableField("role")
    private UserRole role;
    
    @TableField("name")
    private String name;
    
    @TableField("photo_url")
    private String photoUrl;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}