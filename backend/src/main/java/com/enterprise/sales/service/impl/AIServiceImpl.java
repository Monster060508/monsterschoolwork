package com.enterprise.sales.service.impl;

import com.enterprise.sales.service.AIService;
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
            if (isSQLIntent(intent)) {
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
    public String generateAnswer(String question, String context) {
        // 通用问答模板
        if (context != null && !context.isEmpty()) {
            return "根据相关文档信息，" + context;
        } else {
            return "您好！关于您的问题，我目前主要支持以下功能：\n\n" +
                   "1. 销售数据查询与分析\n" +
                   "2. 商品库存信息查询\n" +
                   "3. 订单状态查询\n" +
                   "4. 员工信息查询\n" +
                   "5. 统计报表分析\n\n" +
                   "请您尝试提出以上相关的问题，我会为您提供详细的分析。";
        }
    }
    
    // 私有辅助方法
    
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
    
    private void saveConversationHistory(String conversationId, String role, String content, String intent) {
        String sql = "INSERT INTO conversation_history (conversation_id, user_id, role, content, intent, create_time) VALUES (?, 1, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, conversationId, role, content, intent, LocalDateTime.now());
    }
    
    private String analyzeDataWithAI(String question, List<Map<String, Object>> data) {
        // 基于数据生成自然语言分析
        if (data == null || data.isEmpty()) {
            return "抱歉，没有查询到与您问题相关的数据。";
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("根据查询结果，共找到").append(data.size()).append("条记录：\n\n");
        
        int maxRows = Math.min(5, data.size());
        for (int i = 0; i < maxRows; i++) {
            Map<String, Object> row = data.get(i);
            analysis.append(i + 1).append(". ");
            
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String colName = formatColumnName(entry.getKey());
                String value = formatCellValue(entry.getKey(), entry.getValue());
                parts.add(colName + "：" + value);
            }
            analysis.append(String.join("，", parts));
            analysis.append("\n");
        }
        
        if (data.size() > maxRows) {
            analysis.append("\n（共").append(data.size()).append("条记录，仅展示前").append(maxRows).append("条）");
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
        // 获取所有对话历史列表（按对话ID分组）- MySQL语法
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
            final String intent = analyzeIntent(question);
            
            // 2. 预先获取数据（SQL查询）
            final String sqlQuery;
            final List<Map<String, Object>> sqlData;
            
            if (isSQLIntent(intent)) {
                // 先执行SQL查询，获取真实数据
                sqlQuery = generateSQL(question);
                List<Map<String, Object>> tempData;
                try {
                    tempData = executeSQL(sqlQuery);
                    log.info("SQL查询完成，返回{}条记录", tempData != null ? tempData.size() : 0);
                } catch (Exception e) {
                    log.error("SQL查询失败: {}", sqlQuery, e);
                    tempData = new ArrayList<>();
                }
                sqlData = tempData;
            } else {
                sqlQuery = null;
                sqlData = null;
            }
            
            // 3. 构建包含真实数据的聊天消息
            List<ChatMessage> messages = buildChatMessagesWithData(conversationId, question, intent, sqlData, sqlQuery);
            
            // 4. 保存用户消息
            saveConversationHistory(conversationId, "user", question, intent);
            
            // 5. 流式调用大模型
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
                    
                    // 附加数据信息
                    if (isSQLIntent(intent)) {
                        metadata.put("sql", sqlQuery);
                        metadata.put("data", sqlData != null ? sqlData : new ArrayList<>());
                        metadata.put("path", "SQL");
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
     * 构建包含数据的聊天消息列表
     */
    private List<ChatMessage> buildChatMessagesWithData(String conversationId, String question, String intent, 
                                                         List<Map<String, Object>> sqlData, String sqlQuery) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 系统提示词
        String systemPrompt = buildSystemPromptWithData(intent, sqlData, sqlQuery);
        messages.add(new SystemMessage(systemPrompt));
        
        // 获取历史对话（最近3轮，保持上下文简洁）
        List<Map<String, Object>> history = getConversationHistory(conversationId);
        int start = Math.max(0, history.size() - 6); // 最近6条消息（3轮对话）
        
        for (int i = start; i < history.size(); i++) {
            Map<String, Object> msg = history.get(i);
            String role = msg.get("role").toString();
            String content = msg.get("content").toString();
            
            if ("user".equals(role)) {
                // 用户消息直接加入
                messages.add(new UserMessage(content));
            } else if ("assistant".equals(role)) {
                // AI回复：截取摘要避免过长的SQL数据干扰
                if (content.length() > 300) {
                    content = content.substring(0, 300) + "...";
                }
                messages.add(new AiMessage(content));
            }
        }
        
        // 当前问题
        messages.add(new UserMessage(question));
        
        return messages;
    }
    
    /**
     * 构建包含数据的系统提示词
     */
    private String buildSystemPromptWithData(String intent, List<Map<String, Object>> sqlData, String sqlQuery) {
        StringBuilder prompt = new StringBuilder();
        
        // 角色设定
        prompt.append("你是企业销售管理系统的AI数据分析助手。\n\n");
        
        // 回答规范
        prompt.append("## 回答规范\n");
        prompt.append("- 使用自然流畅的中文，语句完整通顺\n");
        prompt.append("- 用简洁的段落回答，不要罗列原始数据字段名\n");
        prompt.append("- 将英文字段名翻译为中文后呈现给用户\n");
        prompt.append("- 金额以元为单位，保留两位小数\n");
        prompt.append("- 如果数据为空，告知用户暂无相关数据\n\n");
        
        // 如果有SQL数据，格式化后放入提示
        if (isSQLIntent(intent) && sqlData != null && !sqlData.isEmpty()) {
            prompt.append("## 查询结果\n");
            prompt.append(formatDataForAI(sqlData));
            prompt.append("\n\n请基于以上数据，用自然语言回答用户的问题。\n");
        }
        
        return prompt.toString();
    }
    
    /**
     * 格式化数据为AI易读的中文文本
     */
    private String formatDataForAI(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "暂无数据";
        }
        
        StringBuilder sb = new StringBuilder();
        int maxRows = Math.min(10, data.size());
        
        for (int i = 0; i < maxRows; i++) {
            Map<String, Object> row = data.get(i);
            sb.append(i + 1).append(". ");
            
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String colName = formatColumnName(entry.getKey());
                String value = formatCellValue(entry.getKey(), entry.getValue());
                parts.add(colName + "：" + value);
            }
            sb.append(String.join("，", parts));
            sb.append("\n");
        }
        
        if (data.size() > maxRows) {
            sb.append("（共").append(data.size()).append("条记录，仅展示前").append(maxRows).append("条）\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化数据为易读的Markdown文本
     */
    private String formatDataForDisplay(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "无数据";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 获取列名
        Map<String, Object> firstRow = data.get(0);
        List<String> columns = new ArrayList<>(firstRow.keySet());
        
        // 构建Markdown表格
        // 表头
        sb.append("| ");
        for (String col : columns) {
            sb.append(formatColumnName(col)).append(" | ");
        }
        sb.append("\n| ");
        for (int i = 0; i < columns.size(); i++) {
            sb.append("--- | ");
        }
        sb.append("\n");
        
        // 数据行（最多显示20行）
        int maxRows = Math.min(20, data.size());
        for (int i = 0; i < maxRows; i++) {
            Map<String, Object> row = data.get(i);
            sb.append("| ");
            for (String col : columns) {
                Object value = row.get(col);
                sb.append(formatCellValue(col, value)).append(" | ");
            }
            sb.append("\n");
        }
        
        if (data.size() > 20) {
            sb.append("\n*（仅显示前20条，共").append(data.size()).append("条数据）*\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化列名为中文
     */
    private String formatColumnName(String columnName) {
        switch (columnName.toLowerCase()) {
            case "order_no": return "订单号";
            case "customer_name": return "客户";
            case "total_amount": return "金额";
            case "status": return "状态";
            case "create_time": return "时间";
            case "salesperson_name": return "销售人员";
            case "order_count": return "订单数";
            case "total_sales": return "销售额";
            case "name": return "名称";
            case "price": return "价格";
            case "stock_quantity": return "库存";
            case "sales_quantity": return "销量";
            case "sales_amount": return "销售额";
            case "count": return "数量";
            case "role": return "角色";
            default: return columnName;
        }
    }
    
    /**
     * 格式化单元格值
     */
    private String formatCellValue(String column, Object value) {
        if (value == null) {
            return "-";
        }
        
        String colLower = column.toLowerCase();
        
        // 金额格式化
        if (colLower.contains("amount") || colLower.contains("price") || colLower.contains("sales")) {
            try {
                double num = Double.parseDouble(value.toString());
                return String.format("¥%.2f", num);
            } catch (NumberFormatException e) {
                return value.toString();
            }
        }
        
        // 状态翻译
        if ("status".equals(colLower)) {
            switch (value.toString()) {
                case "COMPLETED": return "已完成";
                case "PENDING": return "待处理";
                case "PROCESSING": return "处理中";
                case "SHIPPED": return "已发货";
                case "CANCELLED": return "已取消";
                default: return value.toString();
            }
        }
        
        // 角色翻译
        if ("role".equals(colLower)) {
            switch (value.toString()) {
                case "ADMIN": return "管理员";
                case "SALES": return "销售";
                case "MANAGER": return "经理";
                default: return value.toString();
            }
        }
        
        // 时间格式化（截取到分钟）
        if (colLower.contains("time") && value.toString().length() > 16) {
            return value.toString().substring(0, 16);
        }
        
        return value.toString();
    }
    
}