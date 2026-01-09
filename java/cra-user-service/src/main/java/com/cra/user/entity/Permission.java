package com.cra.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_permission")
@EntityListeners(AuditingEntityListener.class)
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String permissionKey; // 权限标识，如：contract:view, contract:create

    @Column(nullable = false, length = 50)
    private String permissionName; // 权限名称，如：查看合同，创建合同

    @Column(length = 200)
    private String description; // 权限描述

    @Column(length = 100)
    private String resourceType; // 资源类型，如：menu, button, api

    @Column(length = 200)
    private String resourcePath; // 资源路径，如：/api/contracts, /contract/list

    @Column(length = 20)
    private String method; // 请求方法，如：GET, POST, PUT, DELETE

    private Integer sort; // 排序字段

    private Integer status; // 0: 禁用, 1: 启用

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(length = 50)
    private String creator;

    @Column(length = 50)
    private String updater;
}