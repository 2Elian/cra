package com.cra.contract.service.impl;

import com.cra.contract.entity.ContractContent;
import com.cra.contract.entity.ContractMain;
import com.cra.contract.entity.ContractVersion;
import com.cra.contract.repository.ContractContentRepository;
import com.cra.contract.repository.ContractMainRepository;
import com.cra.contract.repository.ContractVersionRepository;
import com.cra.contract.service.ContractService;
import com.cra.common.exception.BusinessException;
import com.cra.common.model.Response;
import cn.dev33.satoken.stp.StpUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 什么是事务?
 * 事务是一组必须要么全部成功、要么全部失败的数据库操作单元。它解决的问题是：如何保证数据一致性
 * 举个例子：假设有一个转账操作：A要扣100元的时候，B就能加100元
 * 对应同步执行的代码是：dao.decreaseA(100); --> dao.increaseB(100);
 * 没有事务的情况（危险） --> A 扣钱成功 -- > 此时程序异常 --> B 没加钱 --> 导致的结果就是数据不一致（钱“消失”了）
 * 有事务的情况（安全） --> 两步作为一个整体执行 任一步失败：全部回滚, 数据恢复到执行前状态
 * 事务的 4 个核心特性（ACID） --> Atomicity（原子性）要么全做，要么全不做 /  Consistency（一致性）数据始终合法 / Isolation（隔离性）多事务互不干扰 / Durability（持久性）提交后永久生效
 */

@Service
@Transactional // 类中所有 public 方法默认都开启事务
public class ContractServiceImpl implements ContractService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContractServiceImpl.class);
    // TODO 写到配置文件里面
    private static final String STORAGE_DIR = "G:\\项目成果打包\\合同审查Agent\\test_cache";

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.username}")
    private String ftpUsername;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.base-path}")
    private String ftpBasePath;
    
    @Autowired // 用来告诉 Spring：这个对象我不自己 new，你帮我注入。 --> 等价于private ContractMainRepository contractMainRepository = new ContractMainRepository();
    private ContractMainRepository contractMainRepository;
    
    @Autowired
    private ContractVersionRepository contractVersionRepository;
    
    @Autowired
    private ContractContentRepository contractContentRepository;
    
    private final Tika tika = new Tika();
    /**
     * 创建合同
     *
     * @param contract
     * @param file
     * @return
     */
    @Override
    public Response<ContractMain> createContract(ContractMain contract, MultipartFile file) {
        try {
            // 验证合同编号唯一性
            if (contractMainRepository.findByContractNumber(contract.getContractNumber()).isPresent()) {
                throw new BusinessException(400, "合同编号已存在");
            }
            
            // 设置合同状态的默认值 --> 刚上传的文件是草稿状态, 经过review阶段会变成待审核状态, 人工查看后会给出接受/拒绝状态
            if (contract.getStatus() == null) {
                contract.setStatus(0);
            }
            
            // 获取当前登录用户，如果未登录则使用默认系统用户
            // TODO 生产环境强制要求必须是登录的人员才能创建合同
            String creatorId = StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "system_auto";
            contract.setCreatorId(creatorId);
            if (!StpUtil.isLogin()) {
                logger.warn("当前未登录，使用默认用户system_auto创建合同");
            }
            
            contract.setCreateTime(LocalDateTime.now());
            contract.setUpdateTime(LocalDateTime.now());
            
            // 保存合同基本信息
            ContractMain savedContract = contractMainRepository.save(contract);
            
            // 创建第一个版本
            // TODO 如果是空的话 应该提前校验 抛出异常 不然无法保证数据库的信息同步
            if (file != null && !file.isEmpty()) {
                createContractVersion(savedContract.getId(), file, "初始版本");
            }
            return Response.success("合同创建成功", savedContract);
        } catch (Exception e) {
            logger.error("创建合同失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "创建合同失败: " + e.getMessage());
        }
    }

    /**
     * 更新合同
     *
     * @param contractId
     * @param contract
     * @return
     */
    @Override
    public Response<ContractMain> updateContract(Long contractId, ContractMain contract) {
        ContractMain existingContract = contractMainRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException(404, "合同不存在"));
        
        // 更新合同信息
        existingContract.setContractName(contract.getContractName());
        existingContract.setPartyAId(contract.getPartyAId());
        existingContract.setPartyBId(contract.getPartyBId());
        existingContract.setAmount(contract.getAmount());
        existingContract.setStartDate(contract.getStartDate());
        existingContract.setEndDate(contract.getEndDate());
        existingContract.setCategory(contract.getCategory());
        existingContract.setDepartment(contract.getDepartment());
        existingContract.setRemark(contract.getRemark());
        existingContract.setUpdateTime(LocalDateTime.now());
        
        return Response.success("合同更新成功", contractMainRepository.save(existingContract));
    }

    /**
     * 上传合同文件
     * @param file 上传的文件 只支持.pdf和.docx格式
     * @return
     */
    @Override
    public Response<ContractMain> uploadContractFile(MultipartFile file) {
        try {
            validateFileType(file);
            // 创建一个草稿合同
            ContractMain contract = new ContractMain();
            // 使用文件名作为合同名称（去除扩展名）
            String fileName = file.getOriginalFilename();
            String contractName = fileName != null && fileName.contains(".") 
                    ? fileName.substring(0, fileName.lastIndexOf(".")) 
                    : fileName;
            
            contract.setContractName(contractName);
            // 生成临时合同编号
            contract.setContractNumber("DRAFT-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000));
            contract.setStatus(0); // 草稿
            
            // 获取当前登录用户，如果未登录则使用默认系统用户
            String creatorId = StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "system_auto";
            contract.setCreatorId(creatorId);
            if (!StpUtil.isLogin()) {
                logger.warn("当前未登录，使用默认用户system_auto创建合同");
            }
            
            contract.setCreateTime(LocalDateTime.now());
            contract.setUpdateTime(LocalDateTime.now());
            // 设置默认值防止报错（如果字段是必填的）
            contract.setPartyAId(0L); // 占位
            contract.setPartyBId(0L); // 占位
            
            ContractMain savedContract = contractMainRepository.save(contract);
            
            // 创建初始版本
            createContractVersion(savedContract.getId(), file, "上传文件自动创建");
            logger.info("文件上传成功");
            return Response.success("文件上传成功，已创建合同草稿", savedContract);
        } catch (Exception e) {
            logger.error("上传文件失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "上传文件失败: " + e.getMessage());
        }
    }
    /**
     * 批量上传合同文件
     * @param files 上传的批量文件 只支持.pdf和.docx格式
     * @return
     */
    @Override
    public Response<List<ContractMain>> batchUploadContractFiles(MultipartFile[] files) {
        List<ContractMain> createdContracts = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) continue;
                Response<ContractMain> response = uploadContractFile(file);
                if (response.getData() != null) {
                    createdContracts.add(response.getData());
                }
            } catch (Exception e) {
                String fileName = file.getOriginalFilename();
                logger.error("批量上传中文件 {} 处理失败: {}", fileName, e.getMessage());
                errors.add("文件 " + fileName + " 失败: " + e.getMessage());
            }
        }
        
        if (createdContracts.isEmpty() && !errors.isEmpty()) {
            throw new BusinessException(500, "批量上传全部失败: " + String.join("; ", errors));
        }
        
        if (!errors.isEmpty()) {
            // 部分成功
            return Response.success("批量上传部分成功，失败详情: " + String.join("; ", errors), createdContracts);
        }
        
        return Response.success("批量上传成功", createdContracts);
    }

    /*
    删除合同
     */
    @Override
    public Response<String> deleteContract(Long contractId) {
        ContractMain contract = contractMainRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException(404, "合同不存在"));
        
        // 删除合同所有版本及物理文件
        List<ContractVersion> versions = contractVersionRepository.findByContractId(contractId);
        for (ContractVersion version : versions) {
            // 1. 删除数据库内容
            contractContentRepository.deleteByContractIdAndVersionId(contractId, version.getId());
            
            // 2. 尝试删除物理文件
            String storagePath = version.getStoragePath();
            if (StringUtils.hasText(storagePath)) {
                try {
                    deletePhysicalFile(storagePath);
                } catch (Exception e) {
                    // 仅记录日志，不阻断数据库删除流程
                    logger.error("删除物理文件失败: {} - {}", storagePath, e.getMessage());
                }
            }
            
            contractVersionRepository.delete(version);
        }
        
        // 删除合同
        contractMainRepository.delete(contract);
        
        return Response.success("合同删除成功");
    }

    /**
     * 删除物理文件（支持本地和FTP）
     */
    private void deletePhysicalFile(String storagePath) {
        if (storagePath.startsWith("ftp://")) {
            // FTP文件删除
            deleteFtpFile(storagePath);
        } else {
            // 本地文件删除
            try {
                Files.deleteIfExists(Paths.get(storagePath));
                logger.info("本地文件已删除: {}", storagePath);
            } catch (IOException e) {
                logger.error("本地文件删除异常: {}", e.getMessage());
            }
        }
    }

    private void deleteFtpFile(String storagePath) {
        // 解析文件名: ftp://host:port/basePath/fileName
        // 简单截取最后一个 '/' 后的部分作为文件名
        String fileName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
        
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpHost, ftpPort);
            ftpClient.login(ftpUsername, ftpPassword);
            ftpClient.enterLocalPassiveMode();
            
            if (ftpClient.changeWorkingDirectory(ftpBasePath)) {
                boolean deleted = ftpClient.deleteFile(fileName);
                if (deleted) {
                    logger.info("FTP文件已删除: {}", fileName);
                } else {
                    logger.warn("FTP文件删除失败或文件不存在: {}", fileName);
                }
            }
        } catch (IOException e) {
            logger.error("连接FTP删除文件失败: {}", e.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    /**
     * AI服务-审核合同
     */
    @Override
    public Response<ContractMain> reviewContract(ContractMain contract) {
        // 合同校验

        // ocr服务 - 更改合同修改人为ocr-agent

        return Response.success(contract);
    }

    @Override
    public Response<ContractMain> getContractById(Long contractId) {
        ContractMain contract = contractMainRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException(404, "合同不存在"));
        return Response.success(contract);
    }
    
    @Override
    public Response<ContractMain> getContractByNumber(String contractNumber) {
        ContractMain contract = contractMainRepository.findByContractNumber(contractNumber)
                .orElseThrow(() -> new BusinessException(404, "合同不存在"));
        return Response.success(contract);
    }
    
    @Override
    public Response<Page<ContractMain>> getAllContracts(Pageable pageable) {
        return Response.success(contractMainRepository.findAll(pageable));
    }
    
    @Override
    public Response<Page<ContractMain>> searchContracts(String keyword, Pageable pageable) {
        return Response.success(contractMainRepository.findByContractNameContainingOrContractNumberContaining(
                keyword, keyword, pageable));
    }
    
    @Override
    public Response<List<ContractMain>> getContractsByStatus(Integer status) {
        return Response.success(contractMainRepository.findByStatus(status));
    }
    
    @Override
    public Response<List<ContractMain>> getContractsByCreator(String creatorId) {
        return Response.success(contractMainRepository.findByCreatorId(creatorId));
    }
    
    @Override
    public Response<List<ContractMain>> getContractsByParty(Long partyId) {
        return Response.success(contractMainRepository.findByPartyAIdOrPartyBId(partyId, partyId));
    }
    
    @Override
    public Response<List<ContractMain>> getContractsByCategory(String category) {
        return Response.success(contractMainRepository.findByCategory(category));
    }
    
    @Override
    public Response<List<ContractMain>> getContractsByDepartment(String department) {
        return Response.success(contractMainRepository.findByDepartment(department));
    }

    /**
     * 创建合同版本
     */
    @Override
    public Response<ContractVersion> createContractVersion(Long contractId, MultipartFile file, String remark) {
        try {
            // 验证文件类型
            validateFileType(file);
            // 验证合同存在
            contractMainRepository.findById(contractId)
                    .orElseThrow(() -> new BusinessException(404, "合同不存在"));
            // 读取文件内容用于哈希和文本提取
            byte[] fileBytes = file.getBytes();
            String contentHash = DigestUtils.md5DigestAsHex(fileBytes);
            // 检查是否与现有版本内容重复
            if (contractVersionRepository.findByContentHash(contentHash).isPresent()) {
                throw new BusinessException(400, "文件内容与现有版本重复");
            }
            // 保存文件到磁盘
            // TODO 生产环境应该统一到服务器操作
            String storagePath = saveFileToDisk(file);
            
            // 提取纯文本
            String plainText = extractPlainText(fileBytes, file.getOriginalFilename());
            
            // 计算新版本号
            Integer newVersionNumber = contractVersionRepository.countByContractId(contractId) + 1;
            
            // 创建版本记录
            ContractVersion version = new ContractVersion();
            version.setContractId(contractId);
            version.setVersionNumber(newVersionNumber);
            version.setContentHash(contentHash);
            version.setStoragePath(storagePath); // 设置存储路径
            version.setFileName(file.getOriginalFilename());
            version.setFileType(file.getContentType());
            version.setFileSize(file.getSize());
            
            // 获取当前登录用户，如果未登录则使用默认系统用户
            // TODO 生产环境需从登录用户获取
            String creatorId = StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "system_auto";
            version.setCreatorId(creatorId);
            
            version.setRemark(remark);
            version.setCreateTime(LocalDateTime.now());
            
            ContractVersion savedVersion = contractVersionRepository.save(version);
            
            // 保存合同内容 (MongoDB)
            ContractContent contractContent = new ContractContent();
            contractContent.setContractId(contractId);
            contractContent.setVersionId(savedVersion.getId());
            // contractContent.setContent(content); // 不再存储原始文件内容到MongoDB
            contractContent.setPlainTextContent(plainText);
            contractContent.setHtmlContent(convertToHtml(plainText));
            contractContent.setCreatorId(creatorId);
            contractContent.setCreateTime(LocalDateTime.now());
            contractContent.setUpdateTime(LocalDateTime.now());
            
            contractContentRepository.save(contractContent);
            
            return Response.success("版本创建成功", savedVersion);
        } catch (IOException | TikaException e) {
            logger.error("创建合同版本失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "创建合同版本失败: " + e.getMessage());
        }
    }
    
    @Override
    public Response<ContractVersion> getContractVersion(Long contractId, Integer versionNumber) {
        ContractVersion version = contractVersionRepository.findByContractIdAndVersionNumber(contractId, versionNumber)
                .orElseThrow(() -> new BusinessException(404, "版本不存在"));
        return Response.success(version);
    }
    
    @Override
    public Response<List<ContractVersion>> getContractVersions(Long contractId) {
        return Response.success(contractVersionRepository.findByContractId(contractId));
    }
    
    @Override
    public Response<ContractVersion> getLatestContractVersion(Long contractId) {
        ContractVersion version = contractVersionRepository.findTopByContractIdOrderByVersionNumberDesc(contractId)
                .orElseThrow(() -> new BusinessException(404, "合同无版本记录"));
        return Response.success(version);
    }
    
    @Override
    public Response<Map<String, Object>> compareContractVersions(Long contractId, Integer version1, Integer version2) {
        // 简化实现，实际应该使用专业的文本对比库
        ContractVersion v1 = getContractVersion(contractId, version1).getData();
        ContractVersion v2 = getContractVersion(contractId, version2).getData();
        
        ContractContent content1 = contractContentRepository.findByContractIdAndVersionId(contractId, v1.getId())
                .orElseThrow(() -> new BusinessException(404, "版本内容不存在"));
        ContractContent content2 = contractContentRepository.findByContractIdAndVersionId(contractId, v2.getId())
                .orElseThrow(() -> new BusinessException(404, "版本内容不存在"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("version1", v1);
        result.put("version2", v2);
        result.put("content1", content1.getPlainTextContent());
        result.put("content2", content2.getPlainTextContent());
        result.put("diff", "版本对比功能待实现");
        
        return Response.success(result);
    }
    
    @Override
    public Response<String> getContractContent(Long contractId, Integer versionNumber) {
        ContractVersion version = versionNumber != null 
                ? getContractVersion(contractId, versionNumber).getData()
                : getLatestContractVersion(contractId).getData();
        
        String content = contractContentRepository.findByContractIdAndVersionId(contractId, version.getId())
                .map(ContractContent::getContent)
                .orElseThrow(() -> new BusinessException(404, "合同内容不存在"));
        
        return Response.success(content);
    }
    
    @Override
    public Response<String> getContractPlainText(Long contractId, Integer versionNumber) {
        ContractVersion version = versionNumber != null 
                ? getContractVersion(contractId, versionNumber).getData()
                : getLatestContractVersion(contractId).getData();
        
        String content = contractContentRepository.findByContractIdAndVersionId(contractId, version.getId())
                .map(ContractContent::getPlainTextContent)
                .orElseThrow(() -> new BusinessException(404, "合同内容不存在"));
        
        return Response.success(content);
    }
    
    @Override
    public Response<String> getContractHtmlContent(Long contractId, Integer versionNumber) {
        ContractVersion version = versionNumber != null 
                ? getContractVersion(contractId, versionNumber).getData()
                : getLatestContractVersion(contractId).getData();
        
        String content = contractContentRepository.findByContractIdAndVersionId(contractId, version.getId())
                .map(ContractContent::getHtmlContent)
                .orElseThrow(() -> new BusinessException(404, "合同内容不存在"));
        
        return Response.success(content);
    }
    
    @Override
    public Response<ContractMain> updateContractStatus(Long contractId, Integer status) {
        ContractMain contract = contractMainRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException(404, "合同不存在"));
        
        contract.setStatus(status);
        contract.setUpdateTime(LocalDateTime.now());
        
        return Response.success("合同状态更新成功", contractMainRepository.save(contract));
    }
    
    @Override
    public Response<byte[]> exportContract(Long contractId, Integer versionNumber, String format) {
        try {
            ContractVersion version = versionNumber != null 
                    ? getContractVersion(contractId, versionNumber).getData()
                    : getLatestContractVersion(contractId).getData();
            
            ContractContent content = contractContentRepository.findByContractIdAndVersionId(contractId, version.getId())
                    .orElseThrow(() -> new BusinessException(404, "合同内容不存在"));
            
            byte[] exportContent;
            switch (format.toLowerCase()) {
                case "txt":
                    exportContent = content.getPlainTextContent().getBytes(StandardCharsets.UTF_8);
                    break;
                case "html":
                    exportContent = content.getHtmlContent().getBytes(StandardCharsets.UTF_8);
                    break;
                default:
                    exportContent = content.getContent().getBytes(StandardCharsets.UTF_8);
            }
            
            return Response.success("合同导出成功", exportContent);
        } catch (Exception e) {
            logger.error("导出合同失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "导出合同失败: " + e.getMessage());
        }
    }
    
    @Override
    public Response<List<Map<String, Object>>> searchContractContent(Long contractId, String keyword) {
        ContractVersion latestVersion = getLatestContractVersion(contractId).getData();
        
        ContractContent content = contractContentRepository.findByContractIdAndVersionId(contractId, latestVersion.getId())
                .orElseThrow(() -> new BusinessException(404, "合同内容不存在"));
        
        List<Map<String, Object>> results = new ArrayList<>();
        String plainText = content.getPlainTextContent();
        String lowerCasePlainText = plainText.toLowerCase();
        String lowerCaseKeyword = keyword.toLowerCase();
        
        int index = lowerCasePlainText.indexOf(lowerCaseKeyword);
        while (index != -1) {
            Map<String, Object> result = new HashMap<>();
            result.put("keyword", keyword);
            result.put("position", index);
            
            // 获取更丰富的前后文
            int start = Math.max(0, index - 100);
            int end = Math.min(plainText.length(), index + keyword.length() + 100);
            
            // 高亮匹配的关键词
            String context = plainText.substring(start, end);
            String highlightedContext = context.replaceAll(
                "(?i)" + keyword, 
                "<mark>$0</mark>"
            );
            
            result.put("context", highlightedContext);
            result.put("version", latestVersion.getVersionNumber());
            results.add(result);
            
            index = lowerCasePlainText.indexOf(lowerCaseKeyword, index + 1);
        }
        
        return Response.success("内容搜索完成", results);
    }
    
    @Override
    public Response<List<Map<String, Object>>> searchAllContractContent(String keyword) {
        // 简单实现，实际项目中应使用Elasticsearch进行全文搜索
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 获取所有合同
        List<ContractMain> contracts = contractMainRepository.findAll();
        
        for (ContractMain contract : contracts) {
            try {
                // 获取最新版本内容
                ContractVersion latestVersion = getLatestContractVersion(contract.getId()).getData();
                ContractContent content = contractContentRepository.findByContractIdAndVersionId(contract.getId(), latestVersion.getId())
                        .orElse(null);
                
                if (content != null) {
                    String plainText = content.getPlainTextContent();
                    if (plainText.contains(keyword)) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("contractId", contract.getId());
                        result.put("contractName", contract.getContractName());
                        result.put("contractNumber", contract.getContractNumber());
                        
                        // 获取第一个匹配的上下文
                        int index = plainText.indexOf(keyword);
                        if (index != -1) {
                            int start = Math.max(0, index - 50);
                            int end = Math.min(plainText.length(), index + keyword.length() + 50);
                            result.put("context", plainText.substring(start, end));
                        }
                        
                        results.add(result);
                    }
                }
            } catch (Exception e) {
                logger.error("搜索合同 {} 内容失败: {}", contract.getId(), e.getMessage(), e);
                // 继续搜索其他合同
            }
        }
        
        return Response.success("全局内容搜索完成", results);
    }
    
    // 辅助方法
    private String extractPlainText(byte[] fileBytes, String fileName)
            throws IOException, TikaException {

        Metadata metadata = new Metadata();
        metadata.set("resourceName", fileName);

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            return tika.parseToString(is, metadata);
        }
    }
    
    private String convertToHtml(String plainText) {
        // 简单转换，实际应该使用更复杂的HTML生成逻辑
        return plainText.replaceAll("\\n", "<br>")
                        .replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
    }

    /**
     * 验证文件类型 只支持/pdf和.docx文件
     * @param file
     */
    private void validateFileType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".pdf") && !fileName.toLowerCase().endsWith(".docx"))) {
            throw new BusinessException(400, "不支持的文件类型，仅支持 .pdf 和 .docx");
        }
    }

    /**
     * 保存文件到磁盘
     * @param file
     * @return
     * @throws IOException
     */
    private String saveFileToDisk(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        
        // 生成唯一文件名: UUID + 原始文件名
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        
        // 尝试上传到FTP
        try {
            uploadToFtp(file.getInputStream(), uniqueFileName);
            return "ftp://" + ftpHost + ":" + ftpPort + ftpBasePath + "/" + uniqueFileName;
        } catch (Exception e) {
            logger.error("FTP上传失败，降级存储到本地磁盘: {}", e.getMessage());
            // 降级：保存到本地磁盘
            Path uploadPath = Paths.get(STORAGE_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toAbsolutePath().toString();
        }
    }
    
    private void uploadToFtp(InputStream inputStream, String fileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpHost, ftpPort);
            ftpClient.login(ftpUsername, ftpPassword);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            // 确保目录存在
            if (!ftpClient.changeWorkingDirectory(ftpBasePath)) {
                ftpClient.makeDirectory(ftpBasePath);
                ftpClient.changeWorkingDirectory(ftpBasePath);
            }
            
            // 上传文件
            boolean success = ftpClient.storeFile(fileName, inputStream);
            if (!success) {
                throw new IOException("FTP文件存储失败");
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
}
