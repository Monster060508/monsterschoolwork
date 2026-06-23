package com.enterprise.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.sales.entity.User;
import com.enterprise.sales.enums.UserRole;
import com.enterprise.sales.mapper.UserMapper;
import com.enterprise.sales.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public List<User> findByRole(UserRole role) {
        return userMapper.findByRole(role.getCode());
    }
    
    @Override
    public List<User> findSalespersons() {
        return userMapper.findSalespersons();
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        
        // 保存用户
        userMapper.insert(user);
        
        // 返回用户信息（不包含密码）
        user.setPassword(null);
        return user;
    }
    
    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = getById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新用户信息
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        if (user.getPhotoUrl() != null) {
            existingUser.setPhotoUrl(user.getPhotoUrl());
        }
        
        existingUser.setUpdateTime(LocalDateTime.now());
        
        // 保存更新
        updateById(existingUser);
        
        // 返回用户信息（不包含密码）
        existingUser.setPassword(null);
        return existingUser;
    }
    
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 逻辑删除
        user.setDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        
        return updateById(user);
    }
    
    @Override
    @Transactional
    public boolean resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        return updateById(user);
    }
    
    @Override
    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        return updateById(user);
    }
    
    @Override
    @Transactional
    public String uploadPhoto(Long userId, String photoUrl) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPhotoUrl(photoUrl);
        user.setUpdateTime(LocalDateTime.now());
        
        updateById(user);
        
        return photoUrl;
    }
    
    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("deleted", 0);
        
        return count(queryWrapper) > 0;
    }
    
    @Override
    public long countByRole(UserRole role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", role.getCode());
        queryWrapper.eq("deleted", 0);
        
        return count(queryWrapper);
    }
    
    @Override
    public List<User> getUsers(int page, int size, String keyword, UserRole role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("username", keyword)
                .or()
                .like("name", keyword)
            );
        }
        
        if (role != null) {
            queryWrapper.eq("role", role.getCode());
        }
        
        queryWrapper.orderByDesc("create_time");
        
        // 分页
        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + offset + ", " + size);
        
        return list(queryWrapper);
    }
    
    @Override
    public long getUserCount(String keyword, UserRole role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("username", keyword)
                .or()
                .like("name", keyword)
            );
        }
        
        if (role != null) {
            queryWrapper.eq("role", role.getCode());
        }
        
        return count(queryWrapper);
    }
}