package com.cra.contract.repository;

import com.cra.contract.entity.ContractVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractVersionRepository extends JpaRepository<ContractVersion, Long> {
    
    List<ContractVersion> findByContractId(Long contractId);
    
    Optional<ContractVersion> findByContractIdAndVersionNumber(Long contractId, Integer versionNumber);
    
    Optional<ContractVersion> findTopByContractIdOrderByVersionNumberDesc(Long contractId);
    
    Optional<ContractVersion> findByContentHash(String contentHash);
    
    @Query("SELECT COUNT(cv) FROM ContractVersion cv WHERE cv.contractId = :contractId")
    Integer countByContractId(@Param("contractId") Long contractId);
    
    List<ContractVersion> findByContractIdAndCreatorId(Long contractId, String creatorId);
}
