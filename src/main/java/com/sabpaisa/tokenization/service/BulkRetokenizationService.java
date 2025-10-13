package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.domain.entity.EnhancedToken;
import com.sabpaisa.tokenization.entity.Merchant;
import com.sabpaisa.tokenization.presentation.dto.BulkRetokenizationRequest;
import com.sabpaisa.tokenization.presentation.dto.BulkRetokenizationResponse;
import com.sabpaisa.tokenization.presentation.dto.RetokenizationResult;
import com.sabpaisa.tokenization.repository.EnhancedTokenRepository;
import com.sabpaisa.tokenization.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BulkRetokenizationService {
    
    private final EnhancedTokenRepository tokenRepository;
    private final MerchantRepository merchantRepository;
    private final TokenizationService tokenizationService;
    private final TokenNotificationService notificationService;
    private final AuditService auditService;
    
    @Async("bulkProcessingExecutor")
    public CompletableFuture<BulkRetokenizationResponse> processBulkRetokenization(
            BulkRetokenizationRequest request, String merchantId) {
        
        log.info("Starting bulk retokenization for merchant: {}", merchantId);
        BulkRetokenizationResponse response = new BulkRetokenizationResponse();
        response.setRequestId(generateRequestId());
        response.setStartTime(LocalDateTime.now());
        
        try {
            Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                    .orElseThrow(() -> new RuntimeException("Merchant not found"));
            
            List<EnhancedToken> tokensToProcess = getTokensForRetokenization(request, merchant);
            log.info("Found {} tokens for retokenization", tokensToProcess.size());
            
            List<RetokenizationResult> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            // Process in batches for better performance
            int batchSize = 100;
            for (int i = 0; i < tokensToProcess.size(); i += batchSize) {
                int end = Math.min(i + batchSize, tokensToProcess.size());
                List<EnhancedToken> batch = tokensToProcess.subList(i, end);
                
                List<RetokenizationResult> batchResults = processBatch(batch, request);
                results.addAll(batchResults);
                
                successCount += batchResults.stream().filter(RetokenizationResult::isSuccess).count();
                failureCount += batchResults.stream().filter(r -> !r.isSuccess()).count();
                
                // Update progress
                int progress = (int) ((double) end / tokensToProcess.size() * 100);
                log.info("Bulk retokenization progress: {}%", progress);
            }
            
            response.setTotalProcessed(results.size());
            response.setSuccessCount(successCount);
            response.setFailureCount(failureCount);
            response.setEndTime(LocalDateTime.now());
            response.setResults(results);
            response.setStatus("COMPLETED");
            
            // Send summary notification
            if (request.isSendNotification()) {
                sendBulkProcessingSummary(merchant, response);
            }
            
            // Audit the bulk operation
            auditService.logBulkRetokenization(merchant, response);
            
        } catch (Exception e) {
            log.error("Error in bulk retokenization: ", e);
            response.setStatus("FAILED");
            response.setErrorMessage(e.getMessage());
            response.setEndTime(LocalDateTime.now());
        }
        
        return CompletableFuture.completedFuture(response);
    }
    
    private List<EnhancedToken> getTokensForRetokenization(
            BulkRetokenizationRequest request, Merchant merchant) {
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (request.getSelectionCriteria()) {
            case EXPIRED:
                return tokenRepository.findExpiredTokensByMerchant(merchant.getMerchantId(), now);
                
            case EXPIRING_SOON:
                LocalDateTime expiryThreshold = now.plusDays(request.getDaysBeforeExpiry());
                return tokenRepository.findExpiringTokensByMerchant(
                    merchant.getMerchantId(), now, expiryThreshold);
                    
            case SPECIFIC_PLATFORM:
                return tokenRepository.findTokensByMerchantAndPlatform(
                    merchant.getMerchantId(), request.getPlatformId());
                    
            case SPECIFIC_TOKENS:
                return tokenRepository.findAllById(request.getTokenIds());
                
            case DATE_RANGE:
                return tokenRepository.findTokensByExpiryDateRange(
                    merchant.getMerchantId(), request.getStartDate(), request.getEndDate());
                    
            default:
                throw new IllegalArgumentException("Invalid selection criteria");
        }
    }
    
    private List<RetokenizationResult> processBatch(
            List<EnhancedToken> tokens, BulkRetokenizationRequest request) {
        
        return tokens.stream().map(token -> {
            RetokenizationResult result = new RetokenizationResult();
            result.setOldTokenId(token.getId());
            result.setOldTokenValue(maskToken(token.getTokenValue()));
            result.setCardLast4(token.getCardLast4());
            
            try {
                // Create new token
                EnhancedToken newToken = createNewToken(token, request);
                
                // Deactivate old token
                token.setIsActive(false);
                token.setStatus(EnhancedToken.TokenStatus.REVOKED);
                tokenRepository.save(token);
                
                result.setSuccess(true);
                result.setNewTokenId(newToken.getId());
                result.setNewTokenValue(maskToken(newToken.getTokenValue()));
                result.setMessage("Token successfully renewed");
                
                // Send individual notification if enabled
                // TODO: Implement sendRenewalNotification in TokenNotificationService
                // if (request.isSendNotification() && token.getNotificationEnabled()) {
                //     notificationService.sendRenewalNotification(newToken);
                // }
                
            } catch (Exception e) {
                log.error("Failed to retokenize token {}: ", token.getId(), e);
                result.setSuccess(false);
                result.setMessage("Failed: " + e.getMessage());
            }
            
            return result;
        }).collect(Collectors.toList());
    }
    
    private EnhancedToken createNewToken(EnhancedToken oldToken, BulkRetokenizationRequest request) {
        // Create new token with same card details but new token value
        EnhancedToken newToken = new EnhancedToken();
        
        // Copy relevant fields
        newToken.setMerchant(oldToken.getMerchant());
        newToken.setPlatform(oldToken.getPlatform());
        newToken.setTokenType(oldToken.getTokenType());
        newToken.setCardHash(oldToken.getCardHash());
        newToken.setCardBin(oldToken.getCardBin());
        newToken.setCardLast4(oldToken.getCardLast4());
        newToken.setCardType(oldToken.getCardType());
        newToken.setCardBrand(oldToken.getCardBrand());
        newToken.setMaskedPan(oldToken.getMaskedPan());
        newToken.setCustomerEmail(oldToken.getCustomerEmail());
        newToken.setCustomerPhone(oldToken.getCustomerPhone());
        newToken.setCustomerId(oldToken.getCustomerId());
        
        // Generate new token value
        newToken.setTokenValue(tokenizationService.generateTokenValue());
        
        // Set new expiry based on request or token type default
        if (request.getNewExpiryMonths() != null) {
            newToken.setExpiryDate(LocalDateTime.now().plusMonths(request.getNewExpiryMonths()));
        } else {
            newToken.setExpiryDate(LocalDateTime.now().plusDays(
                oldToken.getTokenType().getDefaultExpiryDays()));
        }
        
        // Set notification preferences
        newToken.setNotificationEnabled(oldToken.getNotificationEnabled());
        newToken.setDaysBeforeExpiryNotification(oldToken.getDaysBeforeExpiryNotification());
        
        return tokenRepository.save(newToken);
    }
    
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return token;
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
    
    private String generateRequestId() {
        return "BULK_" + System.currentTimeMillis() + "_" + 
               java.util.UUID.randomUUID().toString().substring(0, 8);
    }
    
    private void sendBulkProcessingSummary(Merchant merchant, BulkRetokenizationResponse response) {
        // Implementation for sending summary email/notification
        log.info("Sending bulk processing summary to merchant: {}", merchant.getId());
    }
}