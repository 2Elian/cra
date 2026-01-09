package com.cra.user.service.impl;

import com.cra.user.entity.Role;
import com.cra.user.entity.Permission;
import com.cra.user.repository.RoleRepository;
import com.cra.user.repository.PermissionRepository;
import com.cra.user.service.RoleService;
import com.cra.common.model.Response;
import com.cra.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    @Transactional
    public Response<Role> createRole(Role role) {
        // 检查角色名称是否已存在
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new BusinessException(400, "角色名称已存在");
        }

        // 设置默认状态
        role.setStatus(1);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());

        // 保存角色
        Role savedRole = roleRepository.save(role);

        return Response.success(savedRole);
    }

    @Override
    @Transactional
    public Response<Role> updateRole(Long roleId, Role role) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new BusinessException(404, "角色不存在");
        }

        Role existingRole = roleOptional.get();

        // 检查角色名称是否已存在（排除当前角色）
        Optional<Role> roleByName = roleRepository.findByRoleName(role.getRoleName());
        if (roleByName.isPresent() && !roleByName.get().getId().equals(roleId)) {
            throw new BusinessException(400, "角色名称已存在");
        }

        // 更新角色信息
        existingRole.setRoleName(role.getRoleName());
        existingRole.setDescription(role.getDescription());
        existingRole.setUpdateTime(LocalDateTime.now());

        // 保存更新
        Role updatedRole = roleRepository.save(existingRole);

        return Response.success(updatedRole);
    }

    @Override
    @Transactional
    public Response<String> toggleRoleStatus(Long roleId, Integer status) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new BusinessException(404, "角色不存在");
        }

        Role role = roleOptional.get();
        role.setStatus(status);
        role.setUpdateTime(LocalDateTime.now());

        roleRepository.save(role);

        return Response.success(status == 1 ? "角色已启用" : "角色已禁用");
    }

    @Override
    @Transactional
    public Response<String> deleteRole(Long roleId) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new BusinessException(404, "角色不存在");
        }

        // 检查角色是否被使用
        Role role = roleOptional.get();
        if (!role.getPermissions().isEmpty()) {
            throw new BusinessException(400, "角色已分配权限，无法删除");
        }

        roleRepository.deleteById(roleId);

        return Response.success("角色已删除");
    }

    @Override
    public Response<Role> getRoleInfo(Long roleId) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new BusinessException(404, "角色不存在");
        }

        return Response.success(roleOptional.get());
    }

    @Override
    public Response<List<Role>> getRoleList() {
        List<Role> roles = roleRepository.findAll();
        return Response.success(roles);
    }

    @Override
    @Transactional
    public Response<String> assignPermissions(Long roleId, List<Long> permissionIds) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new BusinessException(404, "角色不存在");
        }

        Role role = roleOptional.get();

        // 查询权限列表
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new BusinessException(400, "部分权限不存在");
        }

        // 分配权限
        role.setPermissions(permissions);
        role.setUpdateTime(LocalDateTime.now());

        roleRepository.save(role);

        return Response.success("权限分配成功");
    }

    @Override
    public Response<Map<String, Object>> getRolePermissions(Long roleId) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isEmpty()) {
            throw new BusinessException(404, "角色不存在");
        }

        Role role = roleOptional.get();

        // 获取所有权限列表
        List<Permission> allPermissions = permissionRepository.findAll();

        // 获取角色已分配的权限ID列表
        List<Long> assignedPermissionIds = role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toList());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("role", role);
        result.put("allPermissions", allPermissions);
        result.put("assignedPermissionIds", assignedPermissionIds);

        return Response.success(result);
    }
}