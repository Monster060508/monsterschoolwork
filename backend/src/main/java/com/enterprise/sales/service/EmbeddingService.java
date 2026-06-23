package com.enterprise.sales.service;

import java.util.List;

/**
 * 向量嵌入服务接口
 */
public interface EmbeddingService {
    
    /**
     * 将文本转换为向量嵌入
     * @param text 文本内容
     * @return 向量列表
     */
    List<Float> embed(String text);
    
    /**
     * 批量将文本转换为向量嵌入
     * @param texts 文本列表
     * @return 向量列表的列表
     */
    List<List<Float>> embedBatch(List<String> texts);
    
    /**
     * 计算两个向量的余弦相似度
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 相似度分数 (0-1)
     */
    double cosineSimilarity(List<Float> vec1, List<Float> vec2);
}