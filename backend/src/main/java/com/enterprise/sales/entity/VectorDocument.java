package com.enterprise.sales.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("vector_document")
public class VectorDocument {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("title")
    private String title;
    
    @TableField("content")
    private String content;
    
    @TableField("chunk_index")
    private Integer chunkIndex;
    
    @JsonIgnore
    @TableField("embedding")
    private String embeddingJson;
    
    @TableField("metadata")
    private String metadata;
    
    @TableField("source_doc_id")
    private Long sourceDocId;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private Double similarityScore;
}