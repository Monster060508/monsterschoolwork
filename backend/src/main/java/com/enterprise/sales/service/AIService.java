package com.enterprise.sales.service;

import java.util.List;
import java.util.Map;

public interface AIService {
    
    /**
     * 流式输出回调接口
     */
    interface StreamCallback {
        /**
         * 每个token回调
         */
        void onToken(String token);
        
        /**
         * 完成回调
         */
        void onComplete(String fullResponse, Map<String, Object> metadata);
        
        /**
         * 错误回调
         */
        void onError(String error);
    }
    
    /**
     * 智能问答对话
     * @param question 用户问题
     * @param conversationId 对话ID
     * @return 回答结果
     */
    Map<String, Object> chat(String question, String conversationId);
    
    /**
     * 流式智能问答对话
     * @param question 用户问题
     * @param conversationId 对话ID
     * @param callback 流式回调
     */
    void chatStream(String question, String conversationId, StreamCallback callback);
    
    /**
     * 获取对话历史
     * @param conversationId 对话ID
     * @return 对话历史
     */
    List<Map<String, Object>> getConversationHistory(String conversationId);
    
    /**
     * 清除对话历史
     * @param conversationId 对话ID
     */
    void clearConversationHistory(String conversationId);
    
    /**
     * 获取支持的意图列表
     * @return 意图列表
     */
    List<String> getSupportedIntents();
    
    /**
     * 分析数据
     * @param question 分析问题
     * @param data 待分析数据
     * @return 分析结果
     */
    Map<String, Object> analyzeData(String question, List<Map<String, Object>> data);
    
    /**
     * 意图分析
     * @param question 用户问题
     * @return 意图分类
     */
    String analyzeIntent(String question);
    
    /**
     * 生成SQL语句
     * @param question 用户问题
     * @return SQL语句
     */
    String generateSQL(String question);
    
    /**
     * 执行SQL查询
     * @param sql SQL语句
     * @return 查询结果
     */
    List<Map<String, Object>> executeSQL(String sql);
    
    /**
     * RAG文档检索
     * @param query 查询内容
     * @param topK 返回数量
     * @return 相关文档
     */
    List<Map<String, Object>> searchDocuments(String query, int topK);
    
    /**
     * 生成回答
     * @param question 用户问题
     * @param context 上下文信息
     * @return 回答内容
     */
    String generateAnswer(String question, String context);
    
    /**
     * 获取所有对话历史列表
     * @return 对话历史列表
     */
    List<Map<String, Object>> getConversations();
}