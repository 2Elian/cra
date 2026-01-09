package com.cra.user.repository;

import com.cra.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermissionKey(String permissionKey);
    List<Permission> findByStatus(Integer status);
    List<Permission> findByResourcePathAndMethod(String resourcePath, String method);
    boolean existsByPermissionKey(String permissionKey);
}