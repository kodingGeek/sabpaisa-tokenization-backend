package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.domain.entity.Merchant;
import com.sabpaisa.tokenization.presentation.dto.BulkRetokenizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    public void logBulkRetokenization(Merchant merchant, BulkRetokenizationResponse response) {
        log.info("Bulk retokenization audit - Merchant: {}, RequestId: {}, Total: {}, Success: {}, Failed: {}",
            merchant.getMerchantId(),
            response.getRequestId(),
            response.getTotalProcessed(),
            response.getSuccessCount(),
            response.getFailureCount()
        );
        
        // In production, save to audit table
        // AuditLog auditLog = new AuditLog();
        // auditLog.setEventType("BULK_RETOKENIZATION");
        // auditLog.setMerchant(merchant);
        // auditLog.setDetails(response);
        // auditLogRepository.save(auditLog);
    }
    
    public void logTokenCreation(String merchantId, String tokenId, String platform) {
        log.info("Token created - Merchant: {}, Token: {}, Platform: {}", merchantId, tokenId, platform);
    }
    
    public void logTokenUsage(String merchantId, String tokenId, String transactionType) {
        log.debug("Token used - Merchant: {}, Token: {}, Type: {}", merchantId, tokenId, transactionType);
    }
    
    public void logSecurityEvent(String eventType, String merchantId, String details) {
        log.warn("Security event - Type: {}, Merchant: {}, Details: {}", eventType, merchantId, details);
    }
}