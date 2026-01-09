package com.cra.user.service;

import com.cra.user.entity.Role;
import com.cra.common.model.Response;

import java.util.List;
import java.util.Map;

public interface RoleService {
    // 创建角色
    Response<Role> createRole(Role role);
    
    // 更新角色
    Response<Role> updateRole(Long roleId, Role role);
    
    // 禁用/启用角色
    Response<String> toggleRoleStatus(Long roleId, Integer status);
    
    // 删除角色
    Response<String> deleteRole(Long roleId);
    
    // 获取角色详情
    Response<Role> getRoleInfo(Long roleId);
    
    // 查询角色列表
    Response<List<Role>> getRoleList();
    
    // 为角色分配权限
    Response<String> assignPermissions(Long roleId, List<Long> permissionIds);
    
    // 获取角色权限列表
    Response<Map<String, Object>> getRolePermissions(Long roleId);
}