package com.enterprise.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("markdown_document")
public class MarkdownDocument {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("title")
    private String title;
    
    @TableField("file_name")
    private String fileName;
    
    @TableField("oss_url")
    private String ossUrl;
    
    @TableField("content")
    private String content;
    
    @TableField("file_size")
    private Long fileSize;
    
    @TableField("upload_user_id")
    private Long uploadUserId;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    
    // 关联查询上传用户信息
    @TableField(exist = false)
    private User uploadUser;
}