package com.enterprise.sales.controller;

import com.enterprise.sales.service.OSSService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private final OSSService ossService;
    
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return ResponseEntity.badRequest().body(createResponse(400, "只支持JPG/PNG格式图片", null));
            }
            
            // 验证文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(createResponse(400, "文件大小不能超过2MB", null));
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String fileName = "images/" + UUID.randomUUID().toString() + extension;
            
            // 上传到OSS
            String url = ossService.uploadFile(fileName, file.getInputStream());
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", fileName);
            data.put("size", file.getSize());
            
            return ResponseEntity.ok(createResponse(200, "上传成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, "上传失败: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(createResponse(400, "文件大小不能超过10MB", null));
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String fileName = "files/" + UUID.randomUUID().toString() + extension;
            
            // 上传到OSS
            String url = ossService.uploadFile(fileName, file.getInputStream());
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", originalFilename);
            data.put("size", file.getSize());
            
            return ResponseEntity.ok(createResponse(200, "上传成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, "上传失败: " + e.getMessage(), null));
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
}
