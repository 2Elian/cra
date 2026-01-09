package com.cra.user.controller;

import com.cra.user.entity.Role;
import com.cra.user.service.RoleService;
import com.cra.common.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public Response<Role> createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    @PutMapping("/{roleId}")
    public Response<Role> updateRole(@PathVariable Long roleId, @RequestBody Role role) {
        return roleService.updateRole(roleId, role);
    }

    @DeleteMapping("/{roleId}")
    public Response<String> deleteRole(@PathVariable Long roleId) {
        return roleService.deleteRole(roleId);
    }

    @GetMapping
    public Response<List<Role>> getAllRoles() {
        return roleService.getRoleList();
    }

    @GetMapping("/{roleId}")
    public Response<Role> getRoleById(@PathVariable Long roleId) {
        return roleService.getRoleInfo(roleId);
    }

    @PutMapping("/{roleId}/status")
    public Response<String> toggleRoleStatus(@PathVariable Long roleId, @RequestParam Integer status) {
        return roleService.toggleRoleStatus(roleId, status);
    }

    @PostMapping("/{roleId}/permissions")
    public Response<String> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        return roleService.assignPermissions(roleId, permissionIds);
    }

    @GetMapping("/{roleId}/permissions")
    public Response<Map<String, Object>> getRolePermissions(@PathVariable Long roleId) {
        return roleService.getRolePermissions(roleId);
    }
}
