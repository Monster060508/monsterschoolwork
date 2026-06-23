package com.enterprise.sales.service.impl;

import com.enterprise.sales.entity.MarkdownDocument;
import com.enterprise.sales.service.AIService;
import com.enterprise.sales.service.MarkdownDocumentService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    
    private final JdbcTemplate jdbcTemplate;
    private final MarkdownDocumentService markdownDocumentService;
    private final StreamingChatLanguageModel streamingChatModel;
    
    @Override
    public Map<String, Object> chat(String question, String conversationId) {
        log.info("收到智能问答请求，问题：{}，对话ID：{}", question, conversationId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 意图分析
            String intent = analyzeIntent(question);
            result.put("intent", intent);
            
            // 2. 根据意图选择处理路径
            if (isRAGIntent(intent)) {
                // RAG路径：文档检索 + 答案生成
                List<Map<String, Object>> documents = searchDocuments(question, 5);
                String context = buildContextFromDocuments(documents);
                String answer = generateAnswer(question, context);
                
                result.put("answer", answer);
                result.put("documents", documents);
                result.put("path", "RAG");
                
            } else if (isSQLIntent(intent)) {
                // SQL路径：SQL生成 + 数据库查询 + AI分析
                String sql = generateSQL(question);
                result.put("sql", sql);
                
                List<Map<String, Object>> data = executeSQL(sql);
                result.put("data", data);
                
                String analysis = analyzeDataWithAI(question, data);
                result.put("answer", analysis);
                result.put("path", "SQL");
                
            } else {
                // 通用问答
                String answer = generateAnswer(question, "");
                result.put("answer", answer);
                result.put("path", "GENERAL");
            }
            
            // 3. 保存对话历史
            saveConversationHistory(conversationId, "user", question, intent);
            saveConversationHistory(conversationId, "assistant", result.get("answer").toString(), intent);
            
            result.put("conversationId", conversationId);
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("智能问答处理失败", e);
            result.put("answer", "抱歉，处理您的问题时出现错误，请稍后重试。");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getConversationHistory(String conversationId) {
        String sql = "SELECT role, content, intent, create_time FROM conversation_history WHERE conversation_id = ? ORDER BY create_time";
        
        return jdbcTemplate.queryForList(sql, conversationId);
    }
    
    @Override
    public void clearConversationHistory(String conversationId) {
        String sql = "DELETE FROM conversation_history WHERE conversation_id = ?";
        jdbcTemplate.update(sql, conversationId);
    }
    
    @Override
    public List<String> getSupportedIntents() {
        return Arrays.asList(
            "sales_query",      // 销售数据查询
            "product_query",    // 商品数据查询
            "order_query",      // 订单数据查询
            "employee_query",   // 员工数据查询
            "statistics",       // 统计分析
            "comparison",       // 对比分析
            "trend",           // 趋势分析
            "general"          // 通用问答
        );
    }
    
    @Override
    public Map<String, Object> analyzeData(String question, List<Map<String, Object>> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String analysis = analyzeDataWithAI(question, data);
            result.put("analysis", analysis);
            result.put("data", data);
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("数据分析失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public String analyzeIntent(String question) {
        // 简单的意图识别逻辑
        String lowerQuestion = question.toLowerCase();
        
        if (containsAny(lowerQuestion, "销售", "业绩", "销冠", "排名", "排行榜")) {
            return "sales_query";
        } else if (containsAny(lowerQuestion, "商品", "产品", "库存", "热门", "销量")) {
            return "product_query";
        } else if (containsAny(lowerQuestion, "订单", "下单", "客户", "完成", "取消")) {
            return "order_query";
        } else if (containsAny(lowerQuestion, "员工", "销售员", "人员", "团队")) {
            return "employee_query";
        } else if (containsAny(lowerQuestion, "统计", "汇总", "总计", "总额")) {
            return "statistics";
        } else if (containsAny(lowerQuestion, "对比", "比较", " versus ", "vs")) {
            return "comparison";
        } else if (containsAny(lowerQuestion, "趋势", "变化", "增长", "下降", "走势")) {
            return "trend";
        } else {
            return "general";
        }
    }
    
    @Override
    public String generateSQL(String question) {
        // 根据问题生成SQL语句
        String intent = analyzeIntent(question);
        
        switch (intent) {
            case "sales_query":
                return generateSalesSQL(question);
            case "product_query":
                return generateProductSQL(question);
            case "order_query":
                return generateOrderSQL(question);
            case "employee_query":
                return generateEmployeeSQL(question);
            case "statistics":
                return generateStatisticsSQL(question);
            default:
                return generateDefaultSQL(question);
        }
    }
    
    @Override
    public List<Map<String, Object>> executeSQL(String sql) {
        try {
            log.info("执行SQL查询：{}", sql);
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("SQL执行失败：{}", sql, e);
            throw new RuntimeException("SQL执行失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> searchDocuments(String query, int topK) {
        // 从Markdown文档表中检索
        List<MarkdownDocument> documents = markdownDocumentService.searchByTitle(query);
        
        // 转换为Map格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (MarkdownDocument doc : documents) {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", doc.getId());
            docMap.put("title", doc.getTitle());
            docMap.put("content", doc.getContent());
            docMap.put("fileName", doc.getFileName());
            docMap.put("ossUrl", doc.getOssUrl());
            docMap.put("createTime", doc.getCreateTime());
            result.add(docMap);
            
            // 限制返回数量
            if (result.size() >= topK) {
                break;
            }
        }
        
        return result;
    }
    
    @Override
    public String generateAnswer(String question, String context) {
        // 简化实现：基于模板生成回答
        // 实际应该调用大模型API
        
        if (context != null && !context.isEmpty()) {
            return "根据相关文档信息，" + context;
        } else {
            return "感谢您的提问。关于\"" + question + "\"，我正在学习相关知识，暂时无法给出准确回答。请稍后再试或联系管理员。";
        }
    }
    
    // 私有辅助方法
    
    private boolean isRAGIntent(String intent) {
        return Arrays.asList("general", "comparison", "trend").contains(intent);
    }
    
    private boolean isSQLIntent(String intent) {
        return Arrays.asList("sales_query", "product_query", "order_query", "employee_query", "statistics").contains(intent);
    }
    
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private String buildContextFromDocuments(List<Map<String, Object>> documents) {
        StringBuilder context = new StringBuilder();
        for (Map<String, Object> doc : documents) {
            if (doc.containsKey("content")) {
                context.append(doc.get("content").toString()).append("\n");
            }
        }
        return context.toString();
    }
    
    private void saveConversationHistory(String conversationId, String role, String content, String intent) {
        String sql = "INSERT INTO conversation_history (conversation_id, user_id, role, content, intent, create_time) VALUES (?, 1, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, conversationId, role, content, intent, LocalDateTime.now());
    }
    
    private String analyzeDataWithAI(String question, List<Map<String, Object>> data) {
        // 简化实现：基于数据生成简单分析
        if (data == null || data.isEmpty()) {
            return "没有查询到相关数据。";
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("根据查询结果，");
        
        if (data.size() == 1) {
            Map<String, Object> row = data.get(0);
            analysis.append("查询到1条记录：");
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                analysis.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
            }
            analysis.delete(analysis.length() - 2, analysis.length());
        } else {
            analysis.append("查询到").append(data.size()).append("条记录。");
            // 显示前3条数据
            for (int i = 0; i < Math.min(3, data.size()); i++) {
                Map<String, Object> row = data.get(i);
                analysis.append("\n").append(i + 1).append(". ");
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    analysis.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
                }
                analysis.delete(analysis.length() - 2, analysis.length());
            }
        }
        
        return analysis.toString();
    }
    
    private String generateSalesSQL(String question) {
        if (question.contains("本月")) {
            return "SELECT u.name as salesperson_name, SUM(o.total_amount) as total_sales, COUNT(o.id) as order_count " +
                   "FROM orders o JOIN sys_user u ON o.salesperson_id = u.id " +
                   "WHERE o.status = 'COMPLETED' AND MONTH(o.create_time) = MONTH(CURRENT_DATE()) " +
                   "GROUP BY u.id, u.name ORDER BY total_sales DESC";
        } else if (question.contains("销冠") || question.contains("最高")) {
            return "SELECT u.name as salesperson_name, SUM(o.total_amount) as total_sales " +
                   "FROM orders o JOIN sys_user u ON o.salesperson_id = u.id " +
                   "WHERE o.status = 'COMPLETED' GROUP BY u.id, u.name ORDER BY total_sales DESC LIMIT 1";
        } else {
            return "SELECT u.name as salesperson_name, SUM(o.total_amount) as total_sales, COUNT(o.id) as order_count " +
                   "FROM orders o JOIN sys_user u ON o.salesperson_id = u.id " +
                   "WHERE o.status = 'COMPLETED' GROUP BY u.id, u.name ORDER BY total_sales DESC";
        }
    }
    
    private String generateProductSQL(String question) {
        if (question.contains("热门") || question.contains("销量")) {
            return "SELECT p.name, SUM(oi.quantity) as sales_quantity, SUM(oi.quantity * oi.unit_price) as sales_amount " +
                   "FROM order_item oi JOIN product p ON oi.product_id = p.id " +
                   "JOIN orders o ON oi.order_id = o.id AND o.status = 'COMPLETED' " +
                   "GROUP BY p.id, p.name ORDER BY sales_quantity DESC LIMIT 10";
        } else if (question.contains("库存")) {
            return "SELECT name, stock_quantity FROM product WHERE stock_quantity < 10 ORDER BY stock_quantity";
        } else {
            return "SELECT name, price, stock_quantity FROM product ORDER BY create_time DESC";
        }
    }
    
    private String generateOrderSQL(String question) {
        if (question.contains("本月")) {
            return "SELECT order_no, customer_name, total_amount, status, create_time " +
                   "FROM orders WHERE MONTH(create_time) = MONTH(CURRENT_DATE()) ORDER BY create_time DESC";
        } else if (question.contains("完成")) {
            return "SELECT order_no, customer_name, total_amount, create_time " +
                   "FROM orders WHERE status = 'COMPLETED' ORDER BY create_time DESC";
        } else {
            return "SELECT order_no, customer_name, total_amount, status, create_time " +
                   "FROM orders ORDER BY create_time DESC LIMIT 20";
        }
    }
    
    private String generateEmployeeSQL(String question) {
        return "SELECT name, role, create_time FROM sys_user WHERE deleted = 0 ORDER BY create_time";
    }
    
    private String generateStatisticsSQL(String question) {
        if (question.contains("销售额")) {
            return "SELECT SUM(total_amount) as total_sales, COUNT(*) as order_count FROM orders WHERE status = 'COMPLETED'";
        } else {
            return "SELECT status, COUNT(*) as count FROM orders GROUP BY status";
        }
    }
    
    private String generateDefaultSQL(String question) {
        return "SELECT '暂不支持此类查询' as message";
    }
    
    @Override
    public List<Map<String, Object>> getConversations() {
        // 获取所有对话历史列表（按对话ID分组）
        String sql = "SELECT conversation_id, MAX(create_time) as last_message_time, " +
                     "COUNT(*) as message_count, " +
                     "SUBSTRING_INDEX(GROUP_CONCAT(content ORDER BY create_time DESC), ',', 1) as last_message " +
                     "FROM conversation_history " +
                     "GROUP BY conversation_id " +
                     "ORDER BY last_message_time DESC";
        
        return jdbcTemplate.queryForList(sql);
    }
    
    @Override
    public void chatStream(String question, String conversationId, StreamCallback callback) {
        log.info("收到流式问答请求，问题：{}，对话ID：{}", question, conversationId);
        
        try {
            // 1. 意图分析
            String intent = analyzeIntent(question);
            
            // 2. 获取对话历史
            List<ChatMessage> messages = buildChatMessages(conversationId, question, intent);
            
            // 3. 保存用户消息
            saveConversationHistory(conversationId, "user", question, intent);
            
            // 4. 流式调用大模型
            AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());
            
            // 使用langchain4j 0.25.0的流式API
            streamingChatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    fullResponse.get().append(token);
                    callback.onToken(token);
                }
                
                @Override
                public void onComplete(Response<AiMessage> response) {
                    String answer = fullResponse.toString();
                    
                    // 保存助手回复
                    saveConversationHistory(conversationId, "assistant", answer, intent);
                    
                    // 构建元数据
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("intent", intent);
                    metadata.put("conversationId", conversationId);
                    metadata.put("timestamp", System.currentTimeMillis());
                    
                    // 如果是SQL意图，附加SQL和数据信息
                    if (isSQLIntent(intent)) {
                        String sql = generateSQL(question);
                        List<Map<String, Object>> data = executeSQL(sql);
                        metadata.put("sql", sql);
                        metadata.put("data", data);
                        metadata.put("path", "SQL");
                    } else if (isRAGIntent(intent)) {
                        List<Map<String, Object>> documents = searchDocuments(question, 5);
                        metadata.put("documents", documents);
                        metadata.put("path", "RAG");
                    } else {
                        metadata.put("path", "GENERAL");
                    }
                    
                    callback.onComplete(answer, metadata);
                }
                
                @Override
                public void onError(Throwable error) {
                    log.error("流式聊天失败", error);
                    callback.onError("抱歉，处理您的问题时出现错误：" + error.getMessage());
                }
            });
            
        } catch (Exception e) {
            log.error("流式聊天初始化失败", e);
            callback.onError("抱歉，处理您的问题时出现错误：" + e.getMessage());
        }
    }
    
    /**
     * 构建聊天消息列表
     */
    private List<ChatMessage> buildChatMessages(String conversationId, String question, String intent) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 系统提示词
        String systemPrompt = buildSystemPrompt(intent);
        messages.add(new SystemMessage(systemPrompt));
        
        // 获取历史对话（最近10轮）
        List<Map<String, Object>> history = getConversationHistory(conversationId);
        int start = Math.max(0, history.size() - 20); // 最近20条消息（10轮对话）
        
        for (int i = start; i < history.size(); i++) {
            Map<String, Object> msg = history.get(i);
            String role = msg.get("role").toString();
            String content = msg.get("content").toString();
            
            if ("user".equals(role)) {
                messages.add(new UserMessage(content));
            } else if ("assistant".equals(role)) {
                messages.add(new AiMessage(content));
            }
        }
        
        // 当前问题
        messages.add(new UserMessage(question));
        
        return messages;
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String intent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是企业销售管理系统的智能助手，专门帮助用户分析销售数据、查询订单信息、管理商品库存等。\n\n");
        prompt.append("你的能力包括：\n");
        prompt.append("1. 销售数据查询和分析\n");
        prompt.append("2. 商品信息查询和库存管理\n");
        prompt.append("3. 订单状态查询和统计\n");
        prompt.append("4. 员工信息查询\n");
        prompt.append("5. 数据趋势分析和对比\n\n");
        
        switch (intent) {
            case "sales_query":
                prompt.append("当前用户正在查询销售数据，请提供详细的销售分析，包括销售额、订单数量、销售人员业绩等信息。");
                break;
            case "product_query":
                prompt.append("当前用户正在查询商品信息，请提供商品库存、销量、价格等相关信息。");
                break;
            case "order_query":
                prompt.append("当前用户正在查询订单信息，请提供订单状态、金额、客户信息等相关数据。");
                break;
            case "employee_query":
                prompt.append("当前用户正在查询员工信息，请提供员工的基本信息和业绩数据。");
                break;
            case "statistics":
                prompt.append("当前用户需要统计数据，请提供详细的统计分析和数据汇总。");
                break;
            default:
                prompt.append("请根据用户的问题提供专业、准确的回答。如果涉及数据查询，请说明可以查询哪些数据。");
        }
        
        return prompt.toString();
    }
}