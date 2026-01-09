package com.cra.contract.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "contract_main")
@Data
public class ContractMain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "contract_number", unique = true, nullable = false)
    private String contractNumber; // 合同编号
    
    @Column(name = "contract_name", nullable = false)
    private String contractName; // 合同名称
    
    @Column(name = "party_a_id", nullable = false)
    private Long partyAId; // 甲方ID
    
    @Column(name = "party_b_id", nullable = false)
    private Long partyBId; // 乙方ID
    
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount; // 合同金额
    
    @Column(name = "start_date")
    private LocalDate startDate; // 开始日期
    
    @Column(name = "end_date")
    private LocalDate endDate; // 结束日期
    
    @Column(name = "status", nullable = false)
    private Integer status; // 合同状态 0:草稿状态 1：审核中 2:审核通过 3:审核拒绝
    
    @Column(name = "category")
    private String category; // 合同类型
    
    @Column(name = "department")
    private String department; // 所属部门
    
    @Column(name = "creator_id", nullable = false)
    private String creatorId; // 创建人ID
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime; // 创建时间
    
    @Column(name = "update_time")
    private LocalDateTime updateTime; // 更新时间
    
    @Column(name = "remark")
    private String remark; // 备注
}
