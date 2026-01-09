package com.cra.contract.controller;

import com.cra.contract.entity.ContractMain;
import com.cra.contract.entity.ContractVersion;
import com.cra.contract.service.ContractService;
import com.cra.common.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    
    @Autowired
    private ContractService contractService;
    
    // 合同基本操作 - 创建一张合同
    @PostMapping
    public Response<ContractMain> createContract(@RequestBody ContractMain contract, 
                                                @RequestParam(value = "file", required = false) MultipartFile file) {
        // RequestBody is raw data for json. RequestParam is form-data
        return contractService.createContract(contract, file);
    }

    // 上传合同
    @PostMapping("/upload/single")
    public Response<ContractMain> uploadSingleContract(@RequestParam("file") MultipartFile file) {
        return contractService.uploadContractFile(file);
    }

    // 批量上传合同
    @PostMapping("/upload/batch")
    public Response<List<ContractMain>> uploadBatchContracts(@RequestParam(value = "files", required = false) MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Response.fail(400, "请选择至少一个文件");
        }
        return contractService.batchUploadContractFiles(files);
    }

    // 更新合同
    @PutMapping("/{id}")
    public Response<ContractMain> updateContract(@PathVariable Long id, @RequestBody ContractMain contract) {
        return contractService.updateContract(id, contract);
    }

    // 删除合同
    @DeleteMapping("/{id}")
    public Response<String> deleteContract(@PathVariable Long id) {
        return contractService.deleteContract(id);
    }

    // agent进行合同审查
    @PostMapping("agent/review")
    public Response<ContractMain> reviewContract(@RequestBody ContractMain contract) {
        return contractService.reviewContract(contract);
    }

    // 通过id获取合同信息
    @GetMapping("/{id}")
    public Response<ContractMain> getContractById(@PathVariable Long id) {
        return contractService.getContractById(id);
    }

    // 通过合同编号获取合同信息
    @GetMapping("/number/{number}")
    public Response<ContractMain> getContractByNumber(@PathVariable String number) {
        return contractService.getContractByNumber(number);
    }
    
    // 合同列表查询
    @GetMapping
    public Response<Page<ContractMain>> getAllContracts(@RequestParam(defaultValue = "0") int page, 
                                                       @RequestParam(defaultValue = "10") int size, 
                                                       @RequestParam(defaultValue = "createTime") String sortBy, 
                                                       @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return contractService.getAllContracts(pageable);
    }
    
    @GetMapping("/search")
    public Response<Page<ContractMain>> searchContracts(@RequestParam String keyword, 
                                                       @RequestParam(defaultValue = "0") int page, 
                                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return contractService.searchContracts(keyword, pageable);
    }
    
    @GetMapping("/status/{status}")
    public Response<List<ContractMain>> getContractsByStatus(@PathVariable Integer status) {
        return contractService.getContractsByStatus(status);
    }
    
    @GetMapping("/creator/{creatorId}")
    public Response<List<ContractMain>> getContractsByCreator(@PathVariable String creatorId) {
        return contractService.getContractsByCreator(creatorId);
    }
    
    @GetMapping("/party/{partyId}")
    public Response<List<ContractMain>> getContractsByParty(@PathVariable Long partyId) {
        return contractService.getContractsByParty(partyId);
    }
    
    @GetMapping("/category/{category}")
    public Response<List<ContractMain>> getContractsByCategory(@PathVariable String category) {
        return contractService.getContractsByCategory(category);
    }
    
    @GetMapping("/department/{department}")
    public Response<List<ContractMain>> getContractsByDepartment(@PathVariable String department) {
        return contractService.getContractsByDepartment(department);
    }
    
    // 合同版本管理
    @PostMapping("/{id}/versions")
    public Response<ContractVersion> createContractVersion(@PathVariable Long id, 
                                                          @RequestParam("file") MultipartFile file, 
                                                          @RequestParam(value = "remark", required = false) String remark) {
        return contractService.createContractVersion(id, file, remark);
    }
    
    @GetMapping("/{id}/versions")
    public Response<List<ContractVersion>> getContractVersions(@PathVariable Long id) {
        return contractService.getContractVersions(id);
    }
    
    @GetMapping("/{id}/versions/{version}")
    public Response<ContractVersion> getContractVersion(@PathVariable Long id, @PathVariable Integer version) {
        return contractService.getContractVersion(id, version);
    }
    
    @GetMapping("/{id}/versions/latest")
    public Response<ContractVersion> getLatestContractVersion(@PathVariable Long id) {
        return contractService.getLatestContractVersion(id);
    }
    
    @GetMapping("/{id}/versions/compare")
    public Response<Map<String, Object>> compareContractVersions(@PathVariable Long id, 
                                                               @RequestParam Integer version1, 
                                                               @RequestParam Integer version2) {
        return contractService.compareContractVersions(id, version1, version2);
    }
    
    // 合同内容管理
    @GetMapping("/{id}/content")
    public Response<String> getContractContent(@PathVariable Long id, 
                                              @RequestParam(value = "version", required = false) Integer version) {
        return contractService.getContractContent(id, version);
    }
    
    @GetMapping("/{id}/plain-text")
    public Response<String> getContractPlainText(@PathVariable Long id, 
                                               @RequestParam(value = "version", required = false) Integer version) {
        return contractService.getContractPlainText(id, version);
    }
    
    @GetMapping("/{id}/html")
    public Response<String> getContractHtml(@PathVariable Long id, 
                                           @RequestParam(value = "version", required = false) Integer version) {
        return contractService.getContractHtmlContent(id, version);
    }
    
    // 合同状态管理
    @PutMapping("/{id}/status/{status}")
    public Response<ContractMain> updateContractStatus(@PathVariable Long id, @PathVariable Integer status) {
        return contractService.updateContractStatus(id, status);
    }
    
    // 合同导出
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportContract(@PathVariable Long id, 
                                                @RequestParam(value = "version", required = false) Integer version, 
                                                @RequestParam(value = "format", defaultValue = "txt") String format) {
        byte[] content = contractService.exportContract(id, version, format).getData();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "contract_" + id + "." + format);
        
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
    
    // 合同内容搜索
    @GetMapping("/{id}/search")
    public Response<List<Map<String, Object>>> searchContractContent(@PathVariable Long id, 
                                                                   @RequestParam String keyword) {
        return contractService.searchContractContent(id, keyword);
    }
    
    @GetMapping("/content/search")
    public Response<List<Map<String, Object>>> searchAllContractContent(@RequestParam String keyword) {
        return contractService.searchAllContractContent(keyword);
    }
}
