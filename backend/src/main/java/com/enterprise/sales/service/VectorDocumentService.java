package com.enterprise.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.sales.entity.VectorDocument;

import java.util.List;

public interface VectorDocumentService extends IService<VectorDocument> {
    
    /**
     * 存储文档块及其向量
     * @param document 源文档信息
     * @param chunks 文档块列表
     * @param embeddings 向量列表
     * @return 存储的向量文档列表
     */
    List<VectorDocument> storeDocumentChunks(VectorDocument document, List<String> chunks, List<List<Float>> embeddings);
    
    /**
     * 语义搜索
     * @param queryEmbedding 查询向量
     * @param topK 返回数量
     * @return 相似文档列表（按相似度排序）
     */
    List<VectorDocument> semanticSearch(List<Float> queryEmbedding, int topK);
    
    /**
     * 根据源文档ID删除所有块
     * @param sourceDocId 源文档ID
     */
    void deleteBySourceDocId(Long sourceDocId);
    
    /**
     * 根据源文档ID查找所有块
     * @param sourceDocId 源文档ID
     * @return 文档块列表
     */
    List<VectorDocument> findBySourceDocId(Long sourceDocId);
    
    /**
     * 获取所有文档块
     */
    List<VectorDocument> findAll();
}