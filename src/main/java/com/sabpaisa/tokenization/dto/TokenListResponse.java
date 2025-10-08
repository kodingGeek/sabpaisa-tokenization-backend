package com.sabpaisa.tokenization.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TokenListResponse {
    private List<TokenInfo> tokens;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    // Constructors
    public TokenListResponse() {}
    
    public TokenListResponse(List<TokenInfo> tokens, int totalElements, int totalPages, int currentPage, int pageSize) {
        this.tokens = tokens;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
    
    // Inner class for token info
    public static class TokenInfo {
        private String tokenValue;
        private String maskedPan;
        private String status;
        private String merchantId;
        private String merchantName;
        private int usageCount;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private String tokenizationMode;
        
        // Constructors
        public TokenInfo() {}
        
        public TokenInfo(String tokenValue, String maskedPan, String status, 
                        String merchantId, String merchantName, int usageCount,
                        LocalDateTime createdAt, LocalDateTime expiresAt) {
            this.tokenValue = tokenValue;
            this.maskedPan = maskedPan;
            this.status = status;
            this.merchantId = merchantId;
            this.merchantName = merchantName;
            this.usageCount = usageCount;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
        }
        
        // Getters and setters
        public String getTokenValue() { return tokenValue; }
        public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }
        
        public String getMaskedPan() { return maskedPan; }
        public void setMaskedPan(String maskedPan) { this.maskedPan = maskedPan; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        
        public int getUsageCount() { return usageCount; }
        public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        
        public String getTokenizationMode() { return tokenizationMode; }
        public void setTokenizationMode(String tokenizationMode) { this.tokenizationMode = tokenizationMode; }
    }
    
    // Getters and setters
    public List<TokenInfo> getTokens() { return tokens; }
    public void setTokens(List<TokenInfo> tokens) { this.tokens = tokens; }
    
    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}