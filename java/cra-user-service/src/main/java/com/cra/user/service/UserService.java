package com.cra.user.service;

import com.cra.user.entity.User;
import com.cra.common.model.Response;

import java.util.List;
import java.util.Map;

public interface UserService {
    // 用户注册
    Response<User> register(User user);
    
    // 用户登录
    Response<Map<String, Object>> login(String username, String password);
    
    // 用户登出
    Response<String> logout();
    
    // 获取当前登录用户
    Response<User> getCurrentUser();
    
    // 刷新令牌
    Response<Map<String, Object>> refreshToken();
    
    // 获取用户信息
    Response<User> getUserInfo(Long userId);
    
    // 更新用户信息
    Response<User> updateUserInfo(User user);

    // 管理员更新用户信息
    Response<User> updateUser(Long userId, User user);
    
    // 更新用户密码
    Response<String> updatePassword(String oldPassword, String newPassword);
    
    // 禁用/启用用户
    Response<String> toggleUserStatus(Long userId, Integer status);
    
    // 删除用户
    Response<String> deleteUser(Long userId);
    
    // 分页查询用户列表
    Response<Map<String, Object>> getUserList(Integer page, Integer pageSize, Map<String, Object> params);
    
    // 为用户分配角色
    Response<String> assignRoles(Long userId, List<Long> roleIds);
    
    // 获取用户角色列表
    Response<List<Map<String, Object>>> getUserRoles(Long userId);
}