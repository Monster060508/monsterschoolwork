package com.enterprise.sales;

import com.enterprise.sales.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类单元测试
 */
@DisplayName("JWT工具类测试")
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "enterprise-sales-management-jwt-secret-key-2024-secure-enough-for-hs256");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }
    
    @Test
    @DisplayName("生成Token - 应返回非空字符串")
    void generateToken_shouldReturnNonEmptyString() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }
    
    @Test
    @DisplayName("验证有效Token - 应返回true")
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        
        boolean isValid = jwtUtil.validateToken(token);
        
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("验证无效Token - 应返回false")
    void validateToken_withInvalidToken_shouldReturnFalse() {
        boolean isValid = jwtUtil.validateToken("invalid.token.here");
        
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("验证空Token - 应返回false")
    void validateToken_withEmptyToken_shouldReturnFalse() {
        boolean isValid = jwtUtil.validateToken("");
        
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("从Token获取用户名 - 应返回正确用户名")
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(1L, "testuser", "ADMIN");
        
        String username = jwtUtil.getUsernameFromToken(token);
        
        assertEquals("testuser", username);
    }
    
    @Test
    @DisplayName("从Token获取用户ID - 应返回正确ID")
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtUtil.generateToken(42L, "admin", "ADMIN");
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        assertEquals(42L, userId);
    }
    
    @Test
    @DisplayName("从Token获取角色 - 应返回正确角色")
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtUtil.generateToken(1L, "admin", "SALES");
        
        String role = jwtUtil.getRoleFromToken(token);
        
        assertEquals("SALES", role);
    }
    
    @Test
    @DisplayName("检查未过期Token - 应返回false")
    void isTokenExpired_withFreshToken_shouldReturnFalse() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        
        boolean isExpired = jwtUtil.isTokenExpired(token);
        
        assertFalse(isExpired);
    }
    
    @Test
    @DisplayName("不同用户生成不同Token")
    void generateToken_differentUsers_shouldProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken(1L, "admin", "ADMIN");
        String token2 = jwtUtil.generateToken(2L, "sales001", "SALES");
        
        assertNotEquals(token1, token2);
    }
}
