package com.enterprise.sales;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.sales.entity.User;
import com.enterprise.sales.enums.UserRole;
import com.enterprise.sales.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {
    
    @Mock
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setName("测试用户");
        testUser.setRole(UserRole.ADMIN);
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setDeleted(0);
    }
    
    @Test
    @DisplayName("根据用户名查找用户 - 应返回用户")
    void findByUsername_shouldReturnUser() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        
        User result = userService.findByUsername("testuser");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getName());
        verify(userService).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("根据用户名查找用户 - 用户不存在应返回null")
    void findByUsername_withNonexistentUser_shouldReturnNull() {
        when(userService.findByUsername("nonexistent")).thenReturn(null);
        
        User result = userService.findByUsername("nonexistent");
        
        assertNull(result);
    }
    
    @Test
    @DisplayName("创建用户 - 应返回创建的用户")
    void createUser_shouldReturnCreatedUser() {
        when(userService.createUser(any(User.class))).thenReturn(testUser);
        
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setName("新用户");
        newUser.setRole(UserRole.SALES);
        
        User result = userService.createUser(newUser);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userService).createUser(any(User.class));
    }
    
    @Test
    @DisplayName("创建重复用户名用户 - 应抛出异常")
    void createUser_withDuplicateUsername_shouldThrowException() {
        when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("用户名已存在"));
        
        User duplicateUser = new User();
        duplicateUser.setUsername("testuser");
        duplicateUser.setPassword("password123");
        duplicateUser.setName("重复用户");
        duplicateUser.setRole(UserRole.SALES);
        
        assertThrows(RuntimeException.class, () -> userService.createUser(duplicateUser));
    }
    
    @Test
    @DisplayName("根据ID查找用户 - 应返回用户")
    void findById_shouldReturnUser() {
        when(userService.getById(1L)).thenReturn(testUser);
        
        User result = userService.getById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
    
    @Test
    @DisplayName("更新用户 - 应返回true")
    void updateUser_shouldReturnTrue() {
        when(userService.updateById(any(User.class))).thenReturn(true);
        
        testUser.setName("已更新用户");
        boolean result = userService.updateById(testUser);
        
        assertTrue(result);
        verify(userService).updateById(testUser);
    }
    
    @Test
    @DisplayName("删除用户 - 应返回true")
    void deleteUser_shouldReturnTrue() {
        when(userService.removeById(1L)).thenReturn(true);
        
        boolean result = userService.removeById(1L);
        
        assertTrue(result);
        verify(userService).removeById(1L);
    }
    
    @Test
    @DisplayName("获取用户分页列表 - 应返回分页数据")
    void getUsers_shouldReturnPaginatedData() {
        Page<User> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testUser));
        page.setTotal(1);
        
        when(userService.page(any(Page.class))).thenReturn(page);
        
        Page<User> result = userService.page(new Page<>(1, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1, result.getTotal());
    }
}
