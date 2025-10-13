package com.sabpaisa.tokenization.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class TokenResponse {
    
    private String tokenValue;
    private String maskedPan;
    private String status;
    private LocalDateTime expiresAt;
    private boolean success;
    private String message;
    private String tokenizationMode;
    private Map<String, String> metadata;
    private Integer usageCount;
    
    // Algorithm information
    private String algorithmType;
    private String tokenFormat;
    
    // Merchant information
    private String merchantId;
    private String merchantName;
    
    // Card details
    private String cardBrand;
    private String cardType;
    private String cardLast4;
    private String issuerBank;
    private String issuerCountry;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    
    // Constructors
    public TokenResponse() {}
    
    public TokenResponse(String tokenValue, String maskedPan, String status, 
                        LocalDateTime expiresAt, boolean success, String message) {
        this.tokenValue = tokenValue;
        this.maskedPan = maskedPan;
        this.status = status;
        this.expiresAt = expiresAt;
        this.success = success;
        this.message = message;
    }
    
    // Static factory methods
    public static TokenResponse success(String tokenValue, String maskedPan, 
                                       String status, LocalDateTime expiresAt) {
        return new TokenResponse(tokenValue, maskedPan, status, expiresAt, true, "Tokenization successful");
    }
    
    public static TokenResponse error(String message) {
        return new TokenResponse(null, null, null, null, false, message);
    }
    
    // Getters and Setters
    public String getTokenValue() {
        return tokenValue;
    }
    
    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }
    
    public String getMaskedPan() {
        return maskedPan;
    }
    
    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTokenizationMode() {
        return tokenizationMode;
    }
    
    public void setTokenizationMode(String tokenizationMode) {
        this.tokenizationMode = tokenizationMode;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public Integer getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
    
    // New getters and setters
    public String getAlgorithmType() {
        return algorithmType;
    }
    
    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }
    
    public String getTokenFormat() {
        return tokenFormat;
    }
    
    public void setTokenFormat(String tokenFormat) {
        this.tokenFormat = tokenFormat;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getMerchantName() {
        return merchantName;
    }
    
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    
    public String getCardBrand() {
        return cardBrand;
    }
    
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public String getCardLast4() {
        return cardLast4;
    }
    
    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }
    
    public String getIssuerBank() {
        return issuerBank;
    }
    
    public void setIssuerBank(String issuerBank) {
        this.issuerBank = issuerBank;
    }
    
    public String getIssuerCountry() {
        return issuerCountry;
    }
    
    public void setIssuerCountry(String issuerCountry) {
        this.issuerCountry = issuerCountry;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}