package com.enterprise.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.sales.entity.MarkdownDocument;
import com.enterprise.sales.mapper.MarkdownDocumentMapper;
import com.enterprise.sales.service.MarkdownDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarkdownDocumentServiceImpl extends ServiceImpl<MarkdownDocumentMapper, MarkdownDocument> implements MarkdownDocumentService {
    
    private final MarkdownDocumentMapper markdownDocumentMapper;
    
    @Override
    @Transactional
    public MarkdownDocument uploadDocument(MarkdownDocument document, String content) {
        // 设置默认值
        document.setContent(content);
        document.setCreateTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());
        document.setDeleted(0);
        
        // 保存文档
        markdownDocumentMapper.insert(document);
        
        return document;
    }
    
    @Override
    public List<MarkdownDocument> searchByTitle(String keyword) {
        return markdownDocumentMapper.searchByTitle(keyword);
    }
    
    @Override
    public List<MarkdownDocument> findByUploadUserId(Long userId) {
        return markdownDocumentMapper.findByUploadUserId(userId);
    }
    
    @Override
    public List<MarkdownDocument> findAll() {
        return markdownDocumentMapper.findAll();
    }
    
    @Override
    @Transactional
    public boolean deleteDocument(Long id) {
        MarkdownDocument document = getById(id);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        // 逻辑删除
        document.setDeleted(1);
        document.setUpdateTime(LocalDateTime.now());
        
        return updateById(document);
    }
    
    @Override
    @Transactional
    public MarkdownDocument updateDocument(Long id, MarkdownDocument document, String content) {
        MarkdownDocument existingDocument = getById(id);
        if (existingDocument == null) {
            throw new RuntimeException("文档不存在");
        }
        
        // 更新文档信息
        if (document.getTitle() != null) {
            existingDocument.setTitle(document.getTitle());
        }
        if (document.getFileName() != null) {
            existingDocument.setFileName(document.getFileName());
        }
        if (document.getOssUrl() != null) {
            existingDocument.setOssUrl(document.getOssUrl());
        }
        if (content != null) {
            existingDocument.setContent(content);
        }
        if (document.getFileSize() != null) {
            existingDocument.setFileSize(document.getFileSize());
        }
        
        existingDocument.setUpdateTime(LocalDateTime.now());
        
        // 保存更新
        updateById(existingDocument);
        
        return existingDocument;
    }
    
    @Override
    public String getDocumentContent(Long id) {
        MarkdownDocument document = getById(id);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        return document.getContent();
    }
    
    @Override
    public long countAll() {
        return markdownDocumentMapper.countAll();
    }
}