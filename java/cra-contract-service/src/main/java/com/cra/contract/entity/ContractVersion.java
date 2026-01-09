package com.cra.contract.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_version")
@Data
public class ContractVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "contract_id", nullable = false)
    private Long contractId; // 关联合同ID
    
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber; // 版本号
    
    @Column(name = "content_hash", nullable = false)
    private String contentHash; // 内容哈希
    
    @Column(name = "storage_path", nullable = false)
    private String storagePath; // 存储路径
    
    @Column(name = "file_name", nullable = false)
    private String fileName; // 文件名
    
    @Column(name = "file_type")
    private String fileType; // 文件类型
    
    @Column(name = "file_size")
    private Long fileSize; // 文件大小
    
    @Column(name = "creator_id", nullable = false)
    private String creatorId; // 创建人ID
    
    @Column(name = "remark")
    private String remark; // 版本说明
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime; // 创建时间
}
