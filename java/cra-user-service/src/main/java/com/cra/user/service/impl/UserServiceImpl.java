package com.cra.user.service.impl;

import com.cra.user.entity.User;
import com.cra.user.entity.Role;
import com.cra.user.repository.UserRepository;
import com.cra.user.repository.RoleRepository;
import com.cra.user.service.UserService;
import com.cra.common.model.Response;
import com.cra.common.exception.BusinessException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Response<User> register(User user) {
        // 用户名校验
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 邮箱校验
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException(400, "邮箱已存在");
        }

        // 加密password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认状态
        user.setStatus(1);
        user.setType(0); // TODO: 如果是管理员呢? 这里也给0吗?
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户
        User savedUser = userRepository.save(user);

        return Response.success(savedUser);
    }

    @Override
    public Response<Map<String, Object>> login(String username, String password) {
        // 根据用户名查询用户
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        User user = userOptional.get();

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(401, "用户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 记录最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 使用Sa-Token登录
        StpUtil.login(user.getId());

        // 生成令牌
        String token = StpUtil.getTokenValue();

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);

        return Response.success(result);
    }

    @Override
    public Response<Map<String, Object>> refreshToken() {
        try {
            // 使用Sa-Token登录当前用户，自动刷新令牌
            StpUtil.login(StpUtil.getLoginIdAsLong());
            
            // 获取新的令牌
            String token = StpUtil.getTokenValue();
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            
            return Response.success(result);
        } catch (Exception e) {
            throw new BusinessException(401, "令牌刷新失败：" + e.getMessage());
        }
    }

    @Override
    public Response<User> getUserInfo(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        return Response.success(userOptional.get());
    }

    @Override
    public Response<String> logout() {
        StpUtil.logout();
        return Response.success("登出成功");
    }

    @Override
    public Response<User> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }
        return Response.success(userOptional.get());
    }

    @Override
    @Transactional
    public Response<User> updateUserInfo(User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        User existingUser = userOptional.get();

        // 更新用户信息
        existingUser.setRealName(user.getRealName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setUpdateTime(LocalDateTime.now());

        // 保存更新
        User updatedUser = userRepository.save(existingUser);

        return Response.success(updatedUser);
    }

    /**
     * 管理员更新用户信息（包含密码、类型等敏感字段）
     */
    @Transactional
    public Response<User> updateUser(Long userId, User user) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }
        User existingUser = userOptional.get();
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            Optional<User> existingEmailUser = userRepository.findByEmail(user.getEmail());
            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(userId)) {
                throw new BusinessException(400, "该邮箱已被其他用户使用");
            }
            existingUser.setEmail(user.getEmail());
        }
        if (user.getType() != null) {
            existingUser.setType(user.getType());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 密码加密
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            // existingUser.setPassword(user.getPassword());
        }
        existingUser.setUpdateTime(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);
        return Response.success(updatedUser);
    }

    @Override
    public Response<String> updatePassword(String oldPassword, String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        User user = userOptional.get();

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }

        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());

        // 保存更新
        userRepository.save(user);

        return Response.success("密码更新成功");
    }

    @Override
    @Transactional
    public Response<String> toggleUserStatus(Long userId, Integer status) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        User user = userOptional.get();
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());

        userRepository.save(user);

        return Response.success(status == 1 ? "用户已启用" : "用户已禁用");
    }

    @Override
    @Transactional
    public Response<String> deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        userRepository.deleteById(userId);

        return Response.success("用户已删除");
    }

    @Override
    public Response<Map<String, Object>> getUserList(Integer page, Integer pageSize, Map<String, Object> params) {
        // 这里简化实现，实际应该使用Spring Data JPA的分页查询
        List<User> users = userRepository.findAll();

        Map<String, Object> result = new HashMap<>();
        result.put("total", users.size());
        result.put("users", users);

        return Response.success(result);
    }

    @Override
    @Transactional
    public Response<String> assignRoles(Long userId, List<Long> roleIds) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        User user = userOptional.get();

        // 查询角色列表
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BusinessException(400, "部分角色不存在");
        }

        // 分配角色
        user.setRoles(roles);
        user.setUpdateTime(LocalDateTime.now());

        userRepository.save(user);

        return Response.success("角色分配成功");
    }

    @Override
    public Response<List<Map<String, Object>>> getUserRoles(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        User user = userOptional.get();

        // 转换角色信息
        List<Map<String, Object>> roleList = user.getRoles().stream().map(role -> {
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("id", role.getId());
            roleMap.put("roleName", role.getRoleName());
            roleMap.put("description", role.getDescription());
            return roleMap;
        }).collect(Collectors.toList());

        return Response.success(roleList);
    }
}