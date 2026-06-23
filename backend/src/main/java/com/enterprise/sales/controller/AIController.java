package com.enterprise.sales.controller;

import com.enterprise.sales.service.AIService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        try {
            // 调用AI服务进行对话
            Map<String, Object> result = aiService.chat(request.getQuestion(), request.getConversationId());
            
            return ResponseEntity.ok(createResponse(200, "获取成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getConversationHistory(@PathVariable String conversationId) {
        try {
            // 获取对话历史
            List<Map<String, Object>> history = aiService.getConversationHistory(conversationId);
            
            return ResponseEntity.ok(createResponse(200, "获取成功", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<?> clearConversationHistory(@PathVariable String conversationId) {
        try {
            // 清除对话历史
            aiService.clearConversationHistory(conversationId);
            
            return ResponseEntity.ok(createResponse(200, "清除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @GetMapping("/intents")
    public ResponseEntity<?> getIntents() {
        // 获取支持的意图列表
        List<String> intents = aiService.getSupportedIntents();
        
        return ResponseEntity.ok(createResponse(200, "获取成功", intents));
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeData(@RequestBody AnalyzeRequest request) {
        try {
            // 分析数据
            Map<String, Object> result = aiService.analyzeData(request.getQuestion(), request.getData());
            
            return ResponseEntity.ok(createResponse(200, "分析成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    private Map<String, Object> createResponse(int code, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    @Data
    public static class ChatRequest {
        private String question;
        private String conversationId;
    }
    
    @Data
    public static class AnalyzeRequest {
        private String question;
        private List<Map<String, Object>> data;
    }
}