package com.cra.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity // 实体类会映射数据库中的一张表，默认类名会作为表名
@Table(name = "sys_user") // 表名
@EntityListeners(AuditingEntityListener.class) // 是 Spring Data JPA 提供的 审计监听器，用于自动填充创建时间、更新时间、创建人、更新人等字段。
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 数据库自增

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String realName;

    @Column(length = 200)
    private String avatar;

    private Integer status; // 0: 禁用, 1: 启用

    private Integer type; // 0: 普通用户, 1: 管理员

    @Column(name = "tenant_id")
    private Long tenantId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime; // @CreatedDate 自动填充创建数据的时间

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime; // @LastModifiedDate 自动填充数据的更新时间

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(length = 50)
    private String creator;

    @Column(length = 50)
    private String updater;
}