package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    Optional<Token> findByTokenValue(String tokenValue);
    
    Optional<Token> findByCardHashAndMerchant(String cardHash, Merchant merchant);
    
    List<Token> findByMerchant(Merchant merchant);
    
    Page<Token> findByMerchant(Merchant merchant, Pageable pageable);
    
    List<Token> findByMerchantAndStatus(Merchant merchant, String status);
    
    boolean existsByTokenValue(String tokenValue);
    
    long countByMerchant(Merchant merchant);
    
    long countByMerchantAndStatus(Merchant merchant, String status);
    
    long countByMerchantAndCreatedAtBetween(Merchant merchant, LocalDateTime start, LocalDateTime end);
}