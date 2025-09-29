package com.sabpaisa.tokenization.dto;

import java.util.List;

public class MerchantListResponse {
    private List<MerchantSummary> merchants;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    // Nested DTO for merchant summary
    public static class MerchantSummary {
        private String merchantId;
        private String businessName;
        private String email;
        private String businessType;
        private String status;
        private long activeTokens;
        private String createdAt;
        
        // Constructors
        public MerchantSummary() {}
        
        public MerchantSummary(String merchantId, String businessName, String email, 
                             String businessType, String status, long activeTokens, String createdAt) {
            this.merchantId = merchantId;
            this.businessName = businessName;
            this.email = email;
            this.businessType = businessType;
            this.status = status;
            this.activeTokens = activeTokens;
            this.createdAt = createdAt;
        }
        
        // Getters and setters
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getActiveTokens() { return activeTokens; }
        public void setActiveTokens(long activeTokens) { this.activeTokens = activeTokens; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
    
    // Constructors
    public MerchantListResponse() {}
    
    public MerchantListResponse(List<MerchantSummary> merchants, int totalElements, 
                               int totalPages, int currentPage, int pageSize) {
        this.merchants = merchants;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
    
    // Getters and setters
    public List<MerchantSummary> getMerchants() { return merchants; }
    public void setMerchants(List<MerchantSummary> merchants) { this.merchants = merchants; }
    
    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}