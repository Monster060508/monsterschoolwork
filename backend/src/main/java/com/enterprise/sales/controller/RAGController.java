package com.enterprise.sales.controller;

import com.enterprise.sales.service.RAGService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RAGController {
    
    private final RAGService ragService;
    
    /**
     * 上传并处理文档（完整RAG流程）
     */
    @PostMapping("/documents")
    public ResponseEntity<?> uploadDocument(@RequestBody UploadRAGDocumentRequest request) {
        try {
            Map<String, Object> result = ragService.uploadAndProcessDocument(
                request.getTitle(), 
                request.getContent(), 
                request.getMetadata()
            );
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(createResponse(200, "文档上传处理成功", result));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, result.get("error").toString(), result));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    /**
     * 语义问答
     */
    @PostMapping("/query")
    public ResponseEntity<?> semanticQuery(@RequestBody SemanticQueryRequest request) {
        try {
            Map<String, Object> result = ragService.semanticQuestionAnswer(
                request.getQuestion(), 
                request.getTopK() != null ? request.getTopK() : 3
            );
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(createResponse(200, "问答成功", result));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, result.get("error").toString(), result));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    /**
     * 获取文档块信息
     */
    @GetMapping("/documents/{sourceDocId}/chunks")
    public ResponseEntity<?> getDocumentChunks(@PathVariable Long sourceDocId) {
        try {
            List<Map<String, Object>> chunks = ragService.getDocumentChunks(sourceDocId);
            return ResponseEntity.ok(createResponse(200, "获取成功", chunks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    /**
     * 删除文档
     */
    @DeleteMapping("/documents/{sourceDocId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long sourceDocId) {
        try {
            ragService.deleteDocument(sourceDocId);
            return ResponseEntity.ok(createResponse(200, "删除成功", null));
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
    public static class UploadRAGDocumentRequest {
        private String title;
        private String content;
        private String metadata;
    }
    
    @Data
    public static class SemanticQueryRequest {
        private String question;
        private Integer topK;
    }
}