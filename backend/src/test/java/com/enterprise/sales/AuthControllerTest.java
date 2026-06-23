package com.enterprise.sales;

import com.enterprise.sales.controller.AuthController;
import com.enterprise.sales.entity.User;
import com.enterprise.sales.enums.UserRole;
import com.enterprise.sales.service.UserService;
import com.enterprise.sales.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 认证控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证控制器测试")
class AuthControllerTest {
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserService userService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthController authController;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword("encodedPassword");
        testUser.setName("管理员");
        testUser.setRole(UserRole.ADMIN);
        testUser.setPhotoUrl("https://example.com/admin.jpg");
    }
    
    @Test
    @DisplayName("登录成功 - 应返回token和用户信息")
    void login_withValidCredentials_shouldReturnTokenAndUserInfo() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userService.findByUsername("admin")).thenReturn(testUser);
        when(jwtUtil.generateToken(1L, "admin", "ADMIN")).thenReturn("test-jwt-token");
        
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        
        ResponseEntity<?> response = authController.login(request);
        
        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(200, body.get("code"));
        
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertNotNull(data);
        assertEquals("test-jwt-token", data.get("token"));
        
        Map<String, Object> userInfo = (Map<String, Object>) data.get("user");
        assertNotNull(userInfo);
        assertEquals("ADMIN", userInfo.get("role"));
    }
    
    @Test
    @DisplayName("登录失败 - 错误密码应返回401")
    void login_withInvalidCredentials_shouldReturn401() {
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Bad credentials"));
        
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");
        
        ResponseEntity<?> response = authController.login(request);
        
        assertEquals(400, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(401, body.get("code"));
    }
    
    @Test
    @DisplayName("获取当前用户 - 有效token应返回用户信息")
    void getCurrentUser_withValidToken_shouldReturnUserInfo() {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("valid-token")).thenReturn("admin");
        when(userService.findByUsername("admin")).thenReturn(testUser);
        
        ResponseEntity<?> response = authController.getCurrentUser("Bearer valid-token");
        
        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(200, body.get("code"));
        
        Map<String, Object> userInfo = (Map<String, Object>) body.get("data");
        assertNotNull(userInfo);
        assertEquals("ADMIN", userInfo.get("role"));
    }
    
    @Test
    @DisplayName("获取当前用户 - 无token应返回401")
    void getCurrentUser_withoutToken_shouldReturn401() {
        ResponseEntity<?> response = authController.getCurrentUser(null);
        
        assertEquals(401, response.getStatusCodeValue());
    }
    
    @Test
    @DisplayName("获取当前用户 - 无效token应返回401")
    void getCurrentUser_withInvalidToken_shouldReturn401() {
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);
        
        ResponseEntity<?> response = authController.getCurrentUser("Bearer invalid-token");
        
        assertEquals(401, response.getStatusCodeValue());
    }
    
    @Test
    @DisplayName("登出 - 应返回成功")
    void logout_shouldReturnSuccess() {
        ResponseEntity<?> response = authController.logout();
        
        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(200, body.get("code"));
    }
}
