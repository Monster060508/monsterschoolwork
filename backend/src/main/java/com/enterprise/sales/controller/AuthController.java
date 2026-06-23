package com.enterprise.sales.controller;

import com.enterprise.sales.entity.User;
import com.enterprise.sales.service.UserService;
import com.enterprise.sales.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            // 设置认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = userService.findByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.badRequest().body(createResponse(400, "用户不存在", null));
            }
            
            // 生成真正的JWT Token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("name", user.getName());
            userInfo.put("role", user.getRole().getCode());
            userInfo.put("photoUrl", user.getPhotoUrl());
            
            data.put("user", userInfo);
            
            return ResponseEntity.ok(createResponse(200, "登录成功", data));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(401, "用户名或密码错误", null));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 清除认证信息
        SecurityContextHolder.clearContext();
        
        return ResponseEntity.ok(createResponse(200, "登出成功", null));
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从请求头中获取JWT Token
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(createResponse(401, "未提供认证令牌", null));
            }
            
            String token = authorization.substring(7);
            
            // 验证Token
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).body(createResponse(401, "无效的认证令牌", null));
            }
            
            // 从Token中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(404).body(createResponse(404, "用户不存在", null));
            }
            
            // 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("name", user.getName());
            userInfo.put("role", user.getRole().getCode());
            userInfo.put("photoUrl", user.getPhotoUrl());
            
            return ResponseEntity.ok(createResponse(200, "获取成功", userInfo));
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body(createResponse(401, "认证失败: " + e.getMessage(), null));
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
    public static class LoginRequest {
        private String username;
        private String password;
    }
}