package com.enterprise.sales.controller;

import com.enterprise.sales.service.AIService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
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
    
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        
        // 异步处理流式输出
        String finalConversationId = conversationId;
        new Thread(() -> {
            try {
                // 发送对话ID
                emitter.send(SseEmitter.event()
                        .name("conversationId")
                        .data(finalConversationId));
                
                // 调用流式聊天服务
                aiService.chatStream(request.getQuestion(), finalConversationId, new AIService.StreamCallback() {
                    @Override
                    public void onToken(String token) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("token")
                                    .data(token));
                        } catch (Exception e) {
                            log.error("发送token失败", e);
                        }
                    }
                    
                    @Override
                    public void onComplete(String fullResponse, Map<String, Object> metadata) {
                        try {
                            // 发送完成事件
                            Map<String, Object> completeData = new HashMap<>();
                            completeData.put("fullResponse", fullResponse);
                            completeData.putAll(metadata);
                            
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data(completeData));
                            
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("发送完成事件失败", e);
                            emitter.completeWithError(e);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(error));
                            emitter.completeWithError(new RuntimeException(error));
                        } catch (Exception e) {
                            log.error("发送错误事件失败", e);
                            emitter.completeWithError(e);
                        }
                    }
                });
                
            } catch (Exception e) {
                log.error("流式聊天处理失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("处理失败: " + e.getMessage()));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        }).start();
        
        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时，conversationId: {}", finalConversationId);
            emitter.complete();
        });
        
        emitter.onError(e -> {
            log.error("SSE连接错误，conversationId: {}", finalConversationId, e);
        });
        
        return emitter;
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
    
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations() {
        try {
            // 获取所有对话历史列表
            List<Map<String, Object>> conversations = aiService.getConversations();
            
            return ResponseEntity.ok(createResponse(200, "获取成功", conversations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
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