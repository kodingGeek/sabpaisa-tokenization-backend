package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.domain.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    @Query("SELECT COUNT(t) FROM Token t WHERE t.merchant.merchantId = :merchantId AND t.createdAt > :after")
    Integer countByMerchantIdAndCreatedAtAfter(@Param("merchantId") String merchantId, @Param("after") LocalDateTime after);
    
    @Query("SELECT COUNT(DISTINCT t.cardHash) FROM Token t WHERE t.merchant.merchantId = :merchantId AND t.createdAt > :after")
    Integer countUniqueCardsByMerchantIdAndCreatedAtAfter(@Param("merchantId") String merchantId, @Param("after") LocalDateTime after);
}