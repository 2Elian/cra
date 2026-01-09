package com.cra.user.service.impl;

import com.cra.user.entity.Permission;
import com.cra.user.repository.PermissionRepository;
import com.cra.user.service.PermissionService;
import com.cra.common.model.Response;
import com.cra.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Response<Permission> createPermission(Permission permission) {
        if (permissionRepository.existsByPermissionKey(permission.getPermissionKey())) {
            throw new BusinessException(400, "权限标识已存在");
        }

        // 设置默认状态和时间
        permission.setStatus(1);
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());

        Permission savedPermission = permissionRepository.save(permission);
        return Response.success(savedPermission);
    }

    @Override
    public Response<Permission> updatePermission(Long permissionId, Permission permission) {
        Optional<Permission> existingPermission = permissionRepository.findById(permissionId);
        if (existingPermission.isEmpty()) {
            throw new BusinessException(404, "权限不存在");
        }

        Permission updatedPermission = existingPermission.get();
        updatedPermission.setPermissionKey(permission.getPermissionKey());
        updatedPermission.setResourcePath(permission.getResourcePath());
        updatedPermission.setMethod(permission.getMethod());
        updatedPermission.setPermissionName(permission.getPermissionName());
        updatedPermission.setDescription(permission.getDescription());
        updatedPermission.setStatus(permission.getStatus());
        updatedPermission.setUpdateTime(LocalDateTime.now());

        Permission savedPermission = permissionRepository.save(updatedPermission);
        return Response.success(savedPermission);
    }

    @Override
    public Response<String> deletePermission(Long permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            throw new BusinessException(404, "权限不存在");
        }
        permissionRepository.deleteById(permissionId);
        return Response.success("权限已删除");
    }

    @Override
    public Response<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return Response.success(permissions);
    }

    @Override
    public Response<Permission> getPermissionById(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(404, "权限不存在"));
        return Response.success(permission);
    }

    @Override
    public Response<Permission> getPermissionByKey(String permissionKey) {
        Permission permission = permissionRepository.findByPermissionKey(permissionKey)
                .orElseThrow(() -> new BusinessException(404, "权限不存在"));
        return Response.success(permission);
    }

    @Override
    public Response<List<Permission>> getPermissionsByStatus(Integer status) {
        List<Permission> permissions = permissionRepository.findByStatus(status);
        return Response.success(permissions);
    }

    @Override
    public Response<List<Permission>> getPermissionsByResourcePathAndMethod(String resourcePath, String method) {
        List<Permission> permissions = permissionRepository.findByResourcePathAndMethod(resourcePath, method);
        return Response.success(permissions);
    }
}
