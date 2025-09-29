package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.FraudDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudDetectionEventRepository extends JpaRepository<FraudDetectionEvent, Long> {
    
    List<FraudDetectionEvent> findByMerchantId(String merchantId);
    
    List<FraudDetectionEvent> findByRiskLevel(String riskLevel);
    
    List<FraudDetectionEvent> findByDecision(String decision);
    
    List<FraudDetectionEvent> findByDeviceFingerprint(String deviceFingerprint);
    
    List<FraudDetectionEvent> findByDeviceFingerprintAndIsFalsePositiveFalse(String deviceFingerprint);
    
    @Query("SELECT e FROM FraudDetectionEvent e WHERE e.merchantId = :merchantId AND e.createdAt > :after ORDER BY e.createdAt DESC")
    Optional<FraudDetectionEvent> findLastEventByMerchantId(@Param("merchantId") String merchantId, @Param("after") LocalDateTime after);
    
    @Query("SELECT COUNT(e) FROM FraudDetectionEvent e WHERE e.merchantId = :merchantId AND e.createdAt > :after AND e.decision = 'BLOCK'")
    Integer countFailedAttemptsByMerchantIdAndCreatedAtAfter(@Param("merchantId") String merchantId, @Param("after") LocalDateTime after);
    
    @Query("SELECT e FROM FraudDetectionEvent e WHERE e.createdAt BETWEEN :start AND :end")
    List<FraudDetectionEvent> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT e FROM FraudDetectionEvent e WHERE e.riskScore >= :minScore AND e.manualReviewNotes IS NULL")
    List<FraudDetectionEvent> findEventsNeedingReview(@Param("minScore") int minScore);
    
    @Query("SELECT COUNT(e) FROM FraudDetectionEvent e WHERE e.createdAt > :after AND e.riskLevel IN ('HIGH', 'CRITICAL')")
    Long countHighRiskEvents(@Param("after") LocalDateTime after);
    
    @Query("SELECT AVG(e.riskScore) FROM FraudDetectionEvent e WHERE e.createdAt > :after")
    Double getAverageRiskScore(@Param("after") LocalDateTime after);
    
    @Query("SELECT e.geoCountry, COUNT(e) FROM FraudDetectionEvent e WHERE e.createdAt > :after AND e.decision = 'BLOCK' GROUP BY e.geoCountry ORDER BY COUNT(e) DESC")
    List<Object[]> getTopRiskCountries(@Param("after") LocalDateTime after);
    
    @Query("SELECT e.triggeredRules, COUNT(e) FROM FraudDetectionEvent e WHERE e.createdAt > :after GROUP BY e.triggeredRules")
    List<Object[]> getMostTriggeredRules(@Param("after") LocalDateTime after);
    
    @Query("SELECT HOUR(e.createdAt), COUNT(e) FROM FraudDetectionEvent e WHERE e.createdAt > :after AND e.riskLevel IN ('HIGH', 'CRITICAL') GROUP BY HOUR(e.createdAt)")
    List<Object[]> getHighRiskHourlyDistribution(@Param("after") LocalDateTime after);
}