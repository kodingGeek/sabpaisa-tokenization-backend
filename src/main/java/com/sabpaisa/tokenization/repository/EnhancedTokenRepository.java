package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.domain.entity.EnhancedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface EnhancedTokenRepository extends JpaRepository<EnhancedToken, Long> {
    
    @Query("SELECT t FROM EnhancedToken t WHERE t.merchant.merchantId = :merchantId AND t.expiryDate < :now AND t.isActive = true")
    List<EnhancedToken> findExpiredTokensByMerchant(@Param("merchantId") String merchantId, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM EnhancedToken t WHERE t.merchant.merchantId = :merchantId AND t.expiryDate > :now AND t.expiryDate < :threshold AND t.isActive = true")
    List<EnhancedToken> findExpiringTokensByMerchant(@Param("merchantId") String merchantId, @Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT t FROM EnhancedToken t WHERE t.merchant.merchantId = :merchantId AND t.platform.id = :platformId AND t.isActive = true")
    List<EnhancedToken> findTokensByMerchantAndPlatform(@Param("merchantId") String merchantId, @Param("platformId") Long platformId);
    
    @Query("SELECT t FROM EnhancedToken t WHERE t.merchant.merchantId = :merchantId AND t.expiryDate >= :startDate AND t.expiryDate <= :endDate")
    List<EnhancedToken> findTokensByExpiryDateRange(@Param("merchantId") String merchantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM EnhancedToken t WHERE t.merchant.merchantId = :merchantId AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    long countTokensCreatedInPeriod(@Param("merchantId") String merchantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM EnhancedToken t WHERE t.merchant.merchantId = :merchantId AND t.isActive = true AND t.createdAt < :endDate")
    long countActiveTokensByMerchant(@Param("merchantId") String merchantId, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t.platform.platformName as platform, COUNT(t) as count FROM EnhancedToken t WHERE t.merchant.merchantId = :merchantId AND t.createdAt >= :startDate AND t.createdAt < :endDate GROUP BY t.platform.platformName")
    Map<String, Long> getTokenCountByPlatform(@Param("merchantId") String merchantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM EnhancedToken t WHERE t.notificationEnabled = true AND t.isActive = true AND t.expiryDate <= :expiryThreshold AND t.expiryDate > :now")
    List<EnhancedToken> findTokensNearExpiry(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM EnhancedToken t WHERE t.cardHash = :cardHash AND t.platform.id = :platformId AND t.isActive = true")
    List<EnhancedToken> findActiveTokensByCardAndPlatform(@Param("cardHash") String cardHash, @Param("platformId") Long platformId);
    
    @Query("SELECT t FROM EnhancedToken t WHERE t.cardHash = :cardHash AND t.merchant.merchantId = :merchantId AND t.isActive = true")
    List<EnhancedToken> findActiveTokensByCardAndMerchant(@Param("cardHash") String cardHash, @Param("merchantId") String merchantId);
}