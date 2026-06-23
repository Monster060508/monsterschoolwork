package com.enterprise.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.sales.entity.MarkdownDocument;

import java.util.List;

public interface MarkdownDocumentService extends IService<MarkdownDocument> {
    
    /**
     * 上传Markdown文档
     * @param document 文档信息
     * @param content 文档内容
     * @return 保存后的文档
     */
    MarkdownDocument uploadDocument(MarkdownDocument document, String content);
    
    /**
     * 根据标题搜索文档
     */
    List<MarkdownDocument> searchByTitle(String keyword);
    
    /**
     * 根据上传用户ID查找文档
     */
    List<MarkdownDocument> findByUploadUserId(Long userId);
    
    /**
     * 获取所有文档
     */
    List<MarkdownDocument> findAll();
    
    /**
     * 删除文档（逻辑删除）
     */
    boolean deleteDocument(Long id);
    
    /**
     * 更新文档内容
     */
    MarkdownDocument updateDocument(Long id, MarkdownDocument document, String content);
    
    /**
     * 获取文档内容
     */
    String getDocumentContent(Long id);
    
    /**
     * 统计文档数量
     */
    long countAll();
}