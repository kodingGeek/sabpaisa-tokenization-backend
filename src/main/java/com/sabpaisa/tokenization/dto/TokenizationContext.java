package com.sabpaisa.tokenization.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Context object containing all information needed for tokenization
 */
@Data
@Builder
public class TokenizationContext {
    private String cardNumber;
    private String merchantId;
    private String customerId;
    private String transactionId;
    private String cardBrand;
    private String cardType;
    private Map<String, String> additionalData;
    
    // COF specific fields
    private boolean isCof;
    private String cofContractId;
    private String cofInitialTransactionId;
    
    // Security context
    private String ipAddress;
    private String userAgent;
    private String deviceId;
}