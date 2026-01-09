package com.cra.contract.repository;

import com.cra.contract.entity.ContractContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContractContentRepository extends MongoRepository<ContractContent, String> {
    
    Optional<ContractContent> findByContractIdAndVersionId(Long contractId, Long versionId);
    
    List<ContractContent> findByContractId(Long contractId);
    
    List<ContractContent> findByVersionId(Long versionId);
    
    @Query("{ 'plainTextContent': { $regex: ?0, $options: 'i' } }")
    List<ContractContent> findByPlainTextContentContaining(String keyword);
    
    @Query("{ $and: [ { 'contractId': ?0 }, { 'plainTextContent': { $regex: ?1, $options: 'i' } } ] }")
    List<ContractContent> findByContractIdAndPlainTextContentContaining(Long contractId, String keyword);
    
    void deleteByContractIdAndVersionId(Long contractId, Long versionId);
}
