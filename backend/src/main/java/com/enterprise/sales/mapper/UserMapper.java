package com.enterprise.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.sales.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据角色查找用户列表
     */
    @Select("SELECT * FROM sys_user WHERE role = #{role} AND deleted = 0")
    List<User> findByRole(@Param("role") String role);
    
    /**
     * 查找销售人员列表
     */
    @Select("SELECT * FROM sys_user WHERE role = 'SALES' AND deleted = 0")
    List<User> findSalespersons();
    
    /**
     * 统计各角色用户数量
     */
    @Select("SELECT role, COUNT(*) as count FROM sys_user WHERE deleted = 0 GROUP BY role")
    List<Object> countByRole();
    
    /**
     * 逻辑删除用户（直接更新deleted字段）
     */
    @Update("UPDATE sys_user SET deleted = 1, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int markUserDeleted(@Param("id") Long id);
}