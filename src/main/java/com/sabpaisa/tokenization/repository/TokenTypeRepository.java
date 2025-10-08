package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.domain.entity.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenTypeRepository extends JpaRepository<TokenType, Long> {
    
    Optional<TokenType> findByTypeCode(String typeCode);
    
    List<TokenType> findByIsActiveTrue();
}