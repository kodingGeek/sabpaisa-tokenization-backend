package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.domain.entity.*;
import com.sabpaisa.tokenization.presentation.dto.*;
import com.sabpaisa.tokenization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenMonetizationService {
    
    private final EnhancedTokenRepository tokenRepository;
    private final MerchantRepository merchantRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final PricingPlanRepository pricingPlanRepository;
    private final TokenUsageRepository tokenUsageRepository;
    
    @Value("${app.monetization.enabled:true}")
    private boolean monetizationEnabled;
    
    @Value("${app.monetization.free-tier-tokens:1000}")
    private int freeTierTokens;
    
    @Scheduled(cron = "0 0 1 1 * *") // Run on 1st day of each month at 1 AM
    public void generateMonthlyBilling() {
        if (!monetizationEnabled) {
            return;
        }
        
        log.info("Starting monthly billing generation");
        LocalDate billingMonth = LocalDate.now().minusMonths(1);
        
        List<Merchant> activeMerchants = merchantRepository.findAllActive();
        
        for (Merchant merchant : activeMerchants) {
            try {
                generateMerchantBilling(merchant, billingMonth);
            } catch (Exception e) {
                log.error("Error generating billing for merchant {}: ", merchant.getId(), e);
            }
        }
        
        log.info("Monthly billing generation completed");
    }
    
    private void generateMerchantBilling(Merchant merchant, LocalDate billingMonth) {
        // Get merchant's pricing plan
        PricingPlan pricingPlan = getPricingPlanForMerchant(merchant);
        
        // Calculate token usage for the month
        TokenUsageStats usageStats = calculateTokenUsage(merchant, billingMonth);
        
        // Create billing record
        BillingRecord billing = new BillingRecord();
        billing.setMerchant(merchant);
        billing.setBillingMonth(billingMonth);
        billing.setPricingPlan(pricingPlan);
        
        // Calculate charges based on usage
        BillingCalculation calculation = calculateCharges(usageStats, pricingPlan);
        
        billing.setTotalTokensCreated(usageStats.getTotalTokensCreated());
        billing.setTotalActiveTokens(usageStats.getTotalActiveTokens());
        billing.setTotalTransactions(usageStats.getTotalTransactions());
        
        billing.setTokenCreationCharges(calculation.getTokenCreationCharges());
        billing.setStorageCharges(calculation.getStorageCharges());
        billing.setTransactionCharges(calculation.getTransactionCharges());
        billing.setPlatformCharges(calculation.getPlatformCharges());
        
        billing.setSubtotal(calculation.getSubtotal());
        billing.setTaxAmount(calculation.getTaxAmount());
        billing.setTotalAmount(calculation.getTotalAmount());
        
        billing.setStatus(BillingRecord.BillingStatus.PENDING);
        billing.setDueDate(LocalDate.now().plusDays(30));
        
        billingRecordRepository.save(billing);
        
        // Send billing notification
        sendBillingNotification(merchant, billing);
        
        log.info("Generated billing for merchant {} - Total: {}", 
                merchant.getId(), calculation.getTotalAmount());
    }
    
    private TokenUsageStats calculateTokenUsage(Merchant merchant, LocalDate billingMonth) {
        LocalDateTime startDate = billingMonth.atStartOfDay();
        LocalDateTime endDate = billingMonth.plusMonths(1).atStartOfDay();
        
        TokenUsageStats stats = new TokenUsageStats();
        
        // Count tokens created in the billing period
        stats.setTotalTokensCreated(
            tokenRepository.countTokensCreatedInPeriod(merchant.getId(), startDate, endDate));
        
        // Count active tokens at end of period
        stats.setTotalActiveTokens(
            tokenRepository.countActiveTokensByMerchant(merchant.getId(), endDate));
        
        // Count transactions in period
        stats.setTotalTransactions(
            tokenUsageRepository.countTransactionsInPeriod(merchant.getId(), startDate, endDate));
        
        // Platform-wise breakdown
        Map<String, Long> platformBreakdown = tokenRepository
            .getTokenCountByPlatform(merchant.getId(), startDate, endDate);
        stats.setPlatformBreakdown(platformBreakdown);
        
        return stats;
    }
    
    private BillingCalculation calculateCharges(TokenUsageStats usage, PricingPlan plan) {
        BillingCalculation calc = new BillingCalculation();
        
        // Token creation charges
        long chargeableTokens = Math.max(0, usage.getTotalTokensCreated() - freeTierTokens);
        BigDecimal tokenCreationCharges = plan.getPerTokenCreationPrice()
            .multiply(BigDecimal.valueOf(chargeableTokens));
        calc.setTokenCreationCharges(tokenCreationCharges);
        
        // Storage charges (per active token per month)
        BigDecimal storageCharges = plan.getPerTokenStoragePrice()
            .multiply(BigDecimal.valueOf(usage.getTotalActiveTokens()));
        calc.setStorageCharges(storageCharges);
        
        // Transaction charges
        BigDecimal transactionCharges = plan.getPerTransactionPrice()
            .multiply(BigDecimal.valueOf(usage.getTotalTransactions()));
        calc.setTransactionCharges(transactionCharges);
        
        // Platform charges (additional charge per platform used)
        int platformCount = usage.getPlatformBreakdown().size();
        BigDecimal platformCharges = plan.getPerPlatformPrice()
            .multiply(BigDecimal.valueOf(Math.max(0, platformCount - 1))); // First platform free
        calc.setPlatformCharges(platformCharges);
        
        // Calculate subtotal
        BigDecimal subtotal = tokenCreationCharges
            .add(storageCharges)
            .add(transactionCharges)
            .add(platformCharges);
        calc.setSubtotal(subtotal);
        
        // Apply tax (18% GST)
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.18"));
        calc.setTaxAmount(taxAmount);
        
        // Total amount
        calc.setTotalAmount(subtotal.add(taxAmount));
        
        return calc;
    }
    
    private PricingPlan getPricingPlanForMerchant(Merchant merchant) {
        if (merchant.getPricingPlan() != null) {
            return merchant.getPricingPlan();
        }
        
        // Return default pricing plan
        return pricingPlanRepository.findByPlanCode("DEFAULT")
            .orElseThrow(() -> new RuntimeException("Default pricing plan not found"));
    }
    
    private void sendBillingNotification(Merchant merchant, BillingRecord billing) {
        // Implementation for sending billing notification via email
        log.info("Sending billing notification to merchant: {}", merchant.getId());
    }
    
    public BillingDashboardResponse getMerchantBillingDashboard(String merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        BillingDashboardResponse response = new BillingDashboardResponse();
        
        // Current month usage
        LocalDate currentMonth = LocalDate.now();
        TokenUsageStats currentUsage = calculateTokenUsage(merchant, currentMonth);
        response.setCurrentMonthUsage(currentUsage);
        
        // Estimated charges
        PricingPlan plan = getPricingPlanForMerchant(merchant);
        BillingCalculation estimatedCharges = calculateCharges(currentUsage, plan);
        response.setEstimatedCharges(estimatedCharges);
        
        // Historical billing
        List<BillingRecord> historicalBilling = billingRecordRepository
            .findByMerchantOrderByBillingMonthDesc(merchant);
        response.setHistoricalBilling(historicalBilling);
        
        // Usage trends
        response.setUsageTrends(getUsageTrends(merchant));
        
        return response;
    }
    
    private UsageTrends getUsageTrends(Merchant merchant) {
        UsageTrends trends = new UsageTrends();
        
        // Calculate trends for last 12 months
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(12);
        
        List<MonthlyUsage> monthlyUsages = tokenUsageRepository
            .getMonthlyUsageTrends(merchant.getId(), startDate, endDate);
        
        trends.setMonthlyUsages(monthlyUsages);
        
        // Calculate growth rates
        if (monthlyUsages.size() >= 2) {
            MonthlyUsage latest = monthlyUsages.get(monthlyUsages.size() - 1);
            MonthlyUsage previous = monthlyUsages.get(monthlyUsages.size() - 2);
            
            double tokenGrowth = calculateGrowthRate(
                previous.getTokensCreated(), latest.getTokensCreated());
            double transactionGrowth = calculateGrowthRate(
                previous.getTransactions(), latest.getTransactions());
            
            trends.setTokenCreationGrowthRate(tokenGrowth);
            trends.setTransactionGrowthRate(transactionGrowth);
        }
        
        return trends;
    }
    
    private double calculateGrowthRate(long previous, long current) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((double)(current - previous) / previous) * 100.0;
    }
    
    public void recordTokenUsage(EnhancedToken token, String transactionType) {
        if (!monetizationEnabled || !token.getIsChargeable()) {
            return;
        }
        
        TokenUsage usage = new TokenUsage();
        usage.setToken(token);
        usage.setMerchant(token.getMerchant());
        usage.setPlatform(token.getPlatform());
        usage.setTransactionType(transactionType);
        usage.setUsageTime(LocalDateTime.now());
        
        tokenUsageRepository.save(usage);
        
        // Update token usage count
        token.setUsageCount(token.getUsageCount() + 1);
        token.setLastUsedAt(java.time.Instant.now());
        tokenRepository.save(token);
    }
}