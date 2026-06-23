package com.enterprise.sales.controller;

import com.enterprise.sales.entity.MarkdownDocument;
import com.enterprise.sales.service.MarkdownDocumentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/markdown-documents")
@RequiredArgsConstructor
public class MarkdownDocumentController {
    
    private final MarkdownDocumentService markdownDocumentService;
    
    @GetMapping
    public ResponseEntity<?> getDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        // 获取文档列表
        List<MarkdownDocument> documents;
        long total;
        
        if (keyword != null && !keyword.isEmpty()) {
            documents = markdownDocumentService.searchByTitle(keyword);
            total = documents.size();
        } else {
            documents = markdownDocumentService.findAll();
            total = markdownDocumentService.countAll();
        }
        
        // 简单分页处理
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, documents.size());
        List<MarkdownDocument> pagedDocuments = documents.subList(fromIndex, toIndex);
        
        // 构建分页响应
        Map<String, Object> data = new HashMap<>();
        data.put("records", pagedDocuments);
        data.put("total", total);
        data.put("size", size);
        data.put("current", page);
        data.put("pages", (total + size - 1) / size);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", data));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        MarkdownDocument document = markdownDocumentService.getById(id);
        if (document == null) {
            return ResponseEntity.status(404).body(createResponse(404, "文档不存在", null));
        }
        
        return ResponseEntity.ok(createResponse(200, "获取成功", document));
    }
    
    @PostMapping
    public ResponseEntity<?> uploadDocument(@RequestBody UploadDocumentRequest request) {
        try {
            MarkdownDocument document = new MarkdownDocument();
            document.setTitle(request.getTitle());
            document.setFileName(request.getFileName());
            document.setOssUrl(request.getOssUrl());
            document.setFileSize(request.getFileSize());
            document.setUploadUserId(request.getUploadUserId());
            
            MarkdownDocument uploadedDocument = markdownDocumentService.uploadDocument(document, request.getContent());
            
            return ResponseEntity.ok(createResponse(200, "上传成功", uploadedDocument));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable Long id, @RequestBody UpdateDocumentRequest request) {
        try {
            MarkdownDocument document = new MarkdownDocument();
            document.setTitle(request.getTitle());
            document.setFileName(request.getFileName());
            document.setOssUrl(request.getOssUrl());
            document.setFileSize(request.getFileSize());
            
            MarkdownDocument updatedDocument = markdownDocumentService.updateDocument(id, document, request.getContent());
            
            return ResponseEntity.ok(createResponse(200, "更新成功", updatedDocument));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            boolean success = markdownDocumentService.deleteDocument(id);
            if (success) {
                return ResponseEntity.ok(createResponse(200, "删除成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "删除失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @GetMapping("/{id}/content")
    public ResponseEntity<?> getDocumentContent(@PathVariable Long id) {
        try {
            String content = markdownDocumentService.getDocumentContent(id);
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", content);
            
            return ResponseEntity.ok(createResponse(200, "获取成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getDocumentsByUser(@PathVariable Long userId) {
        List<MarkdownDocument> documents = markdownDocumentService.findByUploadUserId(userId);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", documents));
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
    public static class UploadDocumentRequest {
        private String title;
        private String fileName;
        private String ossUrl;
        private String content;
        private Long fileSize;
        private Long uploadUserId;
    }
    
    @Data
    public static class UpdateDocumentRequest {
        private String title;
        private String fileName;
        private String ossUrl;
        private String content;
        private Long fileSize;
    }
}