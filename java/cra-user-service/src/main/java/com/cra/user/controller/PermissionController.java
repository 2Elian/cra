package com.cra.user.controller;

import com.cra.user.entity.Permission;
import com.cra.user.service.PermissionService;
import com.cra.common.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping
    public Response<Permission> createPermission(@RequestBody Permission permission) {
        return permissionService.createPermission(permission);
    }

    @PutMapping("/{permissionId}")
    public Response<Permission> updatePermission(@PathVariable Long permissionId, @RequestBody Permission permission) {
        return permissionService.updatePermission(permissionId, permission);
    }

    @DeleteMapping("/{permissionId}")
    public Response<String> deletePermission(@PathVariable Long permissionId) {
        return permissionService.deletePermission(permissionId);
    }

    @GetMapping
    public Response<List<Permission>> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @GetMapping("/{permissionId}")
    public Response<Permission> getPermissionById(@PathVariable Long permissionId) {
        return permissionService.getPermissionById(permissionId);
    }

    @GetMapping("/key/{permissionKey}")
    public Response<Permission> getPermissionByKey(@PathVariable String permissionKey) {
        return permissionService.getPermissionByKey(permissionKey);
    }

    @GetMapping("/status/{status}")
    public Response<List<Permission>> getPermissionsByStatus(@PathVariable Integer status) {
        return permissionService.getPermissionsByStatus(status);
    }

    @GetMapping("/resource")
    public Response<List<Permission>> getPermissionsByResourcePathAndMethod(
            @RequestParam String resourcePath, @RequestParam String method) {
        return permissionService.getPermissionsByResourcePathAndMethod(resourcePath, method);
    }
}
