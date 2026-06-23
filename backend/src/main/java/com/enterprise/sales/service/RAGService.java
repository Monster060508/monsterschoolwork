package com.enterprise.sales.service;

import java.util.List;
import java.util.Map;

/**
 * RAG（检索增强生成）服务接口
 */
public interface RAGService {
    
    /**
     * 上传并处理文档（完整RAG流程：上传→分块→向量化→存储）
     * @param title 文档标题
     * @param content 文档内容
     * @param metadata 元数据
     * @return 处理结果
     */
    Map<String, Object> uploadAndProcessDocument(String title, String content, String metadata);
    
    /**
     * 语义问答（完整RAG流程：问题向量化→语义检索→上下文构建→大模型生成）
     * @param question 用户问题
     * @param topK 检索文档数量
     * @return 回答结果
     */
    Map<String, Object> semanticQuestionAnswer(String question, int topK);
    
    /**
     * 获取文档处理状态
     * @param sourceDocId 源文档ID
     * @return 文档块信息
     */
    List<Map<String, Object>> getDocumentChunks(Long sourceDocId);
    
    /**
     * 删除文档及其所有向量块
     * @param sourceDocId 源文档ID
     */
    void deleteDocument(Long sourceDocId);
}