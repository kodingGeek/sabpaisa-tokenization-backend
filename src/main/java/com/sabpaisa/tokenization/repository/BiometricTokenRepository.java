package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.BiometricToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for biometric tokens
 */
@Repository
public interface BiometricTokenRepository extends JpaRepository<BiometricToken, Long> {
    
    /**
     * Find token by token ID
     */
    Optional<BiometricToken> findByTokenId(String tokenId);
    
    /**
     * Find tokens by enrollment ID
     */
    List<BiometricToken> findByEnrollmentId(String enrollmentId);
    
    /**
     * Find tokens by merchant ID
     */
    List<BiometricToken> findByMerchantId(String merchantId);
    
    /**
     * Find active tokens for an enrollment
     */
    @Query("SELECT t FROM BiometricToken t WHERE t.enrollmentId = :enrollmentId " +
           "AND t.status = 'ACTIVE' AND t.expiresAt > :now")
    List<BiometricToken> findActiveTokensByEnrollmentId(
        @Param("enrollmentId") String enrollmentId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find tokens by security level
     */
    List<BiometricToken> findBySecurityLevel(String securityLevel);
    
    /**
     * Find tokens expiring soon
     */
    @Query("SELECT t FROM BiometricToken t WHERE t.expiresAt BETWEEN :start AND :end")
    List<BiometricToken> findTokensExpiringSoon(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    /**
     * Find frequently used tokens
     */
    @Query("SELECT t FROM BiometricToken t WHERE t.usageCount >= :minUsage ORDER BY t.usageCount DESC")
    List<BiometricToken> findFrequentlyUsedTokens(@Param("minUsage") int minUsage);
    
    /**
     * Update token usage statistics
     */
    @Modifying
    @Transactional
    @Query("UPDATE BiometricToken t SET t.usageCount = t.usageCount + 1, " +
           "t.lastUsedAt = :now WHERE t.tokenId = :tokenId")
    void updateUsageStatistics(
        @Param("tokenId") String tokenId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Deactivate expired tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE BiometricToken t SET t.status = 'EXPIRED' WHERE t.expiresAt < :now AND t.status = 'ACTIVE'")
    int deactivateExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Count active tokens by enrollment
     */
    @Query("SELECT COUNT(t) FROM BiometricToken t WHERE t.enrollmentId = :enrollmentId AND t.status = 'ACTIVE'")
    long countActiveTokensByEnrollmentId(@Param("enrollmentId") String enrollmentId);
    
    /**
     * Find tokens with specific required modalities
     */
    @Query("SELECT t FROM BiometricToken t WHERE :modality MEMBER OF t.requiredModalities")
    List<BiometricToken> findByRequiredModality(@Param("modality") String modality);
    
    /**
     * Delete tokens by enrollment ID
     */
    @Modifying
    @Transactional
    void deleteByEnrollmentId(String enrollmentId);
    
    /**
     * Find tokens for audit
     */
    @Query("SELECT t FROM BiometricToken t WHERE t.createdAt BETWEEN :startDate AND :endDate " +
           "AND t.merchantId = :merchantId ORDER BY t.createdAt DESC")
    List<BiometricToken> findTokensForAudit(
        @Param("merchantId") String merchantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}