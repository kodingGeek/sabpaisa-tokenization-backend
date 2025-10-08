package com.sabpaisa.tokenization.dto;

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
}