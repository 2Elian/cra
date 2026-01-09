package com.cra.user.controller;

import com.cra.user.entity.User;
import com.cra.user.service.UserService;
import com.cra.common.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Response<User> register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Response<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        return userService.login(username, password);
    }

    @PostMapping("/logout")
    public Response<String> logout() {
        return userService.logout();
    }

    @GetMapping("/profile")
    public Response<User> getProfile() {
        return userService.getCurrentUser();
    }

    @PutMapping("/profile")
    public Response<User> updateProfile(@RequestBody User user) {
        return userService.updateUserInfo(user);
    }

    /**
     * 管理员更新指定用户信息
     * 对应前端接口：PUT /api/users/{userId}
     */
    @PutMapping("/{userId}")
    public Response<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
        return userService.updateUser(userId, user);
    }

    @PutMapping("/password")
    public Response<String> updatePassword(@RequestBody Map<String, String> passwordData) {
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        return userService.updatePassword(oldPassword, newPassword);
    }

    @GetMapping("/refresh")
    public Response<Map<String, Object>> refreshToken() {
        return userService.refreshToken();
    }

    @GetMapping("/{userId}")
    public Response<User> getUserInfo(@PathVariable Long userId) {
        return userService.getUserInfo(userId);
    }

    @PutMapping("/{userId}/status")
    public Response<String> toggleUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        return userService.toggleUserStatus(userId, status);
    }

    @DeleteMapping("/{userId}")
    public Response<String> deleteUser(@PathVariable Long userId) {
        return userService.deleteUser(userId);
    }

    @GetMapping("/list")
    public Response<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Map<String, Object> params) {
        return userService.getUserList(page, pageSize, params);
    }

    @PostMapping("/{userId}/roles")
    public Response<String> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        return userService.assignRoles(userId, roleIds);
    }

    @GetMapping("/{userId}/roles")
    public Response<List<Map<String, Object>>> getUserRoles(@PathVariable Long userId) {
        return userService.getUserRoles(userId);
    }
}
