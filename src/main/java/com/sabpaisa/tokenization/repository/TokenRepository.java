package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.entity.Merchant;
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
    
    Optional<Token> findByCardHashAndMerchantAndAlgorithmType(String cardHash, Merchant merchant, String algorithmType);
    
    Optional<Token> findByTokenValueAndMerchant_MerchantId(String tokenValue, String merchantId);
    
    // Search tokens with filtering
    @Query("SELECT t FROM Token t WHERE " +
           "(:merchant IS NULL OR t.merchant = :merchant) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:algorithmType IS NULL OR t.algorithmType = :algorithmType) AND " +
           "(:cardBrand IS NULL OR t.cardBrand = :cardBrand) AND " +
           "(:fromDate IS NULL OR t.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR t.createdAt <= :toDate)")
    Page<Token> searchTokens(@Param("merchant") Merchant merchant,
                            @Param("status") String status,
                            @Param("algorithmType") String algorithmType,
                            @Param("cardBrand") String cardBrand,
                            @Param("fromDate") LocalDateTime fromDate,
                            @Param("toDate") LocalDateTime toDate,
                            Pageable pageable);
                            
    // Method to get all tokens
    Page<Token> findAll(Pageable pageable);
}