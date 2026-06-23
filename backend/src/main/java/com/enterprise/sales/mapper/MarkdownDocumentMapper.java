package com.enterprise.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.sales.entity.MarkdownDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MarkdownDocumentMapper extends BaseMapper<MarkdownDocument> {
    
    /**
     * 根据标题搜索文档
     */
    @Select("SELECT * FROM markdown_document WHERE title LIKE CONCAT('%', #{keyword}, '%') AND deleted = 0")
    List<MarkdownDocument> searchByTitle(@Param("keyword") String keyword);
    
    /**
     * 根据上传用户ID查找文档
     */
    @Select("SELECT * FROM markdown_document WHERE upload_user_id = #{userId} AND deleted = 0")
    List<MarkdownDocument> findByUploadUserId(@Param("userId") Long userId);
    
    /**
     * 获取所有文档（按创建时间倒序）
     */
    @Select("SELECT * FROM markdown_document WHERE deleted = 0 ORDER BY create_time DESC")
    List<MarkdownDocument> findAll();
    
    /**
     * 统计文档数量
     */
    @Select("SELECT COUNT(*) FROM markdown_document WHERE deleted = 0")
    long countAll();
}