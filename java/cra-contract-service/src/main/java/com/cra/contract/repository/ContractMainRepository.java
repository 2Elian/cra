package com.cra.contract.repository;

import com.cra.contract.entity.ContractMain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ContractMainRepository extends JpaRepository<ContractMain, Long> {
    
    Optional<ContractMain> findByContractNumber(String contractNumber);
    
    List<ContractMain> findByPartyAIdOrPartyBId(Long partyAId, Long partyBId);
    
    List<ContractMain> findByCreatorId(String creatorId);
    
    List<ContractMain> findByStatus(Integer status);
    
    List<ContractMain> findByCategory(String category);
    
    List<ContractMain> findByDepartment(String department);
    
    Page<ContractMain> findByContractNameContainingOrContractNumberContaining(String contractName, String contractNumber, Pageable pageable);
    
    @Query("SELECT c FROM ContractMain c WHERE c.startDate BETWEEN :startDate AND :endDate")
    List<ContractMain> findByStartDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT c FROM ContractMain c WHERE c.amount BETWEEN :minAmount AND :maxAmount")
    List<ContractMain> findByAmountBetween(@Param("minAmount") Double minAmount, @Param("maxAmount") Double maxAmount);
}
