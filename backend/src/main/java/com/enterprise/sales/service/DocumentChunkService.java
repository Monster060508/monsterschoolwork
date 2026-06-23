package com.enterprise.sales.service;

import java.util.List;

/**
 * 文档分块服务接口
 */
public interface DocumentChunkService {
    
    /**
     * 将文档内容分割成多个块
     * @param content 文档内容
     * @param chunkSize 每块的最大字符数
     * @param overlap 块之间的重叠字符数
     * @return 文档块列表
     */
    List<String> splitDocument(String content, int chunkSize, int overlap);
    
    /**
     * 智能分割文档（按段落和句子分割）
     * @param content 文档内容
     * @param maxChunkSize 最大块大小
     * @return 文档块列表
     */
    List<String> smartSplit(String content, int maxChunkSize);
}