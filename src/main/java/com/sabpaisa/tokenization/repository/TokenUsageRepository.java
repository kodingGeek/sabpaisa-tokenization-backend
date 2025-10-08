package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.domain.entity.TokenUsage;
import com.sabpaisa.tokenization.presentation.dto.MonthlyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TokenUsageRepository extends JpaRepository<TokenUsage, Long> {
    
    @Query("SELECT COUNT(u) FROM TokenUsage u WHERE u.merchant.merchantId = :merchantId AND u.usageTime >= :startDate AND u.usageTime < :endDate")
    long countTransactionsInPeriod(@Param("merchantId") String merchantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query(value = """
        SELECT 
            DATE_FORMAT(u.usageTime, '%Y-%m') as month,
            COUNT(DISTINCT u.token_id) as tokensCreated,
            COUNT(u.id) as transactions,
            COUNT(DISTINCT CASE WHEN t.isActive = true THEN t.id END) as activeTokens
        FROM TokenUsage u
        JOIN EnhancedToken t ON u.token_id = t.id
        WHERE u.merchant.merchantId = :merchantId 
        AND u.usageTime >= :startDate 
        AND u.usageTime <= :endDate
        GROUP BY DATE_FORMAT(u.usageTime, '%Y-%m')
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlyUsageTrendsRaw(@Param("merchantId") String merchantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    default List<MonthlyUsage> getMonthlyUsageTrends(String merchantId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> raw = getMonthlyUsageTrendsRaw(merchantId, startDate, endDate);
        return raw.stream().map(row -> {
            MonthlyUsage usage = new MonthlyUsage();
            // Convert from raw data
            return usage;
        }).toList();
    }
}