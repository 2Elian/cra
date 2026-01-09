package com.cra.user.service;

import com.cra.user.entity.Permission;
import com.cra.common.model.Response;

import java.util.List;

public interface PermissionService {
    Response<Permission> createPermission(Permission permission);
    Response<Permission> updatePermission(Long permissionId, Permission permission);
    Response<String> deletePermission(Long permissionId);
    Response<List<Permission>> getAllPermissions();
    Response<Permission> getPermissionById(Long permissionId);
    Response<Permission> getPermissionByKey(String permissionKey);
    Response<List<Permission>> getPermissionsByStatus(Integer status);
    Response<List<Permission>> getPermissionsByResourcePathAndMethod(String resourcePath, String method);
}
