package com.cra.contract.service;

import com.cra.contract.entity.ContractMain;
import com.cra.contract.entity.ContractVersion;
import com.cra.common.model.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ContractService {
    
    // 合同基本操作 - 创建合同
    Response<ContractMain> createContract(ContractMain contract, MultipartFile file);
    
    // 文件上传接口
    Response<ContractMain> uploadContractFile(MultipartFile file);
    // 批量文件上传接口
    Response<List<ContractMain>> batchUploadContractFiles(MultipartFile[] files);
    // 更新合同接口
    Response<ContractMain> updateContract(Long contractId, ContractMain contract);
    // 合同基本操作 - 删除合同
    Response<String> deleteContract(Long contractId);
    // agent进行合同审查
    Response<ContractMain> reviewContract(ContractMain contract);
    // 通过id获取合同信息
    Response<ContractMain> getContractById(Long contractId);
    // 通过合同编号获取合同信息
    Response<ContractMain> getContractByNumber(String contractNumber);
    // 合同列表查询
    Response<Page<ContractMain>> getAllContracts(Pageable pageable);
    
    Response<Page<ContractMain>> searchContracts(String keyword, Pageable pageable);
    
    Response<List<ContractMain>> getContractsByStatus(Integer status);
    
    Response<List<ContractMain>> getContractsByCreator(String creatorId);
    
    Response<List<ContractMain>> getContractsByParty(Long partyId);
    
    Response<List<ContractMain>> getContractsByCategory(String category);
    
    Response<List<ContractMain>> getContractsByDepartment(String department);
    
    // 合同版本管理
    Response<ContractVersion> createContractVersion(Long contractId, MultipartFile file, String remark);
    
    Response<ContractVersion> getContractVersion(Long contractId, Integer versionNumber);
    
    Response<List<ContractVersion>> getContractVersions(Long contractId);
    
    Response<ContractVersion> getLatestContractVersion(Long contractId);
    
    Response<Map<String, Object>> compareContractVersions(Long contractId, Integer version1, Integer version2);
    
    // 合同内容管理
    Response<String> getContractContent(Long contractId, Integer versionNumber);
    
    Response<String> getContractPlainText(Long contractId, Integer versionNumber);
    
    Response<String> getContractHtmlContent(Long contractId, Integer versionNumber);
    
    // 合同状态管理
    Response<ContractMain> updateContractStatus(Long contractId, Integer status);
    
    // 合同导出
    Response<byte[]> exportContract(Long contractId, Integer versionNumber, String format);
    
    // 合同搜索
    Response<List<Map<String, Object>>> searchContractContent(Long contractId, String keyword);
    
    Response<List<Map<String, Object>>> searchAllContractContent(String keyword);
}
