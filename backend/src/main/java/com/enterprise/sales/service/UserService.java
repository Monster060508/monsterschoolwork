package com.enterprise.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.sales.entity.User;
import com.enterprise.sales.enums.UserRole;

import java.util.List;

public interface UserService extends IService<User> {
    
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 根据角色查找用户列表
     */
    List<User> findByRole(UserRole role);
    
    /**
     * 查找销售人员列表
     */
    List<User> findSalespersons();
    
    /**
     * 创建用户
     */
    User createUser(User user);
    
    /**
     * 更新用户信息
     */
    User updateUser(Long id, User user);
    
    /**
     * 删除用户（逻辑删除）
     */
    boolean deleteUser(Long id);
    
    /**
     * 重置用户密码
     */
    boolean resetPassword(Long id, String newPassword);
    
    /**
     * 修改用户密码
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);
    
    /**
     * 上传用户照片
     */
    String uploadPhoto(Long userId, String photoUrl);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 统计各角色用户数量
     */
    long countByRole(UserRole role);
    
    /**
     * 获取用户列表（支持分页和搜索）
     */
    List<User> getUsers(int page, int size, String keyword, UserRole role);
    
    /**
     * 获取用户总数
     */
    long getUserCount(String keyword, UserRole role);
}