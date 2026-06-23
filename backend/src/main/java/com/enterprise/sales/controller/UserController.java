package com.enterprise.sales.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.sales.entity.User;
import com.enterprise.sales.enums.UserRole;
import com.enterprise.sales.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role) {
        
        // 获取用户列表
        List<User> users = userService.getUsers(page, size, keyword, role);
        long total = userService.getUserCount(keyword, role);
        
        // 构建分页响应
        Map<String, Object> data = new HashMap<>();
        data.put("records", users);
        data.put("total", total);
        data.put("size", size);
        data.put("current", page);
        data.put("pages", (total + size - 1) / size);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", data));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.status(404).body(createResponse(404, "用户不存在", null));
        }
        
        // 清除密码
        user.setPassword(null);
        
        return ResponseEntity.ok(createResponse(200, "获取成功", user));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createResponse(200, "创建成功", createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(createResponse(200, "更新成功", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return ResponseEntity.ok(createResponse(200, "删除成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "删除失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        try {
            boolean success = userService.resetPassword(id, request.getNewPassword());
            if (success) {
                return ResponseEntity.ok(createResponse(200, "密码重置成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "密码重置失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        try {
            boolean success = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
            if (success) {
                return ResponseEntity.ok(createResponse(200, "密码修改成功", null));
            } else {
                return ResponseEntity.badRequest().body(createResponse(400, "密码修改失败", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @PostMapping("/{id}/upload-photo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id, @RequestBody UploadPhotoRequest request) {
        try {
            String photoUrl = userService.uploadPhoto(id, request.getPhotoUrl());
            
            Map<String, Object> data = new HashMap<>();
            data.put("url", photoUrl);
            
            return ResponseEntity.ok(createResponse(200, "上传成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(400, e.getMessage(), null));
        }
    }
    
    @GetMapping("/salespersons")
    public ResponseEntity<?> getSalespersons() {
        List<User> salespersons = userService.findSalespersons();
        
        // 清除密码
        salespersons.forEach(user -> user.setPassword(null));
        
        return ResponseEntity.ok(createResponse(200, "获取成功", salespersons));
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
    public static class ResetPasswordRequest {
        private String newPassword;
    }
    
    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
    
    @Data
    public static class UploadPhotoRequest {
        private String photoUrl;
    }
}