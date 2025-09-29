package com.sabpaisa.tokenization.dto;

import java.time.LocalDateTime;

public class MerchantResponse {
    private String merchantId;
    private String businessName;
    private String email;
    private String phoneNumber;
    private String businessType;
    private String businessAddress;
    private String panNumber;
    private String gstNumber;
    private String status;
    private String webhookUrl;
    private ApiCredentials apiCredentials;
    private MerchantSettings settings;
    private MerchantStats stats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Nested DTO for API credentials
    public static class ApiCredentials {
        private String apiKey;
        private String apiKeyHint; // Last 4 characters of API key
        
        public ApiCredentials(String apiKey) {
            this.apiKey = apiKey;
            if (apiKey != null && apiKey.length() > 4) {
                this.apiKeyHint = "****" + apiKey.substring(apiKey.length() - 4);
            }
        }
        
        // Getters and setters
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        
        public String getApiKeyHint() { return apiKeyHint; }
        public void setApiKeyHint(String apiKeyHint) { this.apiKeyHint = apiKeyHint; }
    }
    
    // Nested DTO for merchant settings
    public static class MerchantSettings {
        private boolean allowRefunds;
        private boolean allowPartialRefunds;
        private int tokenExpiryDays;
        private int maxTokensPerCard;
        private boolean notifyOnTokenCreation;
        
        // Getters and setters
        public boolean isAllowRefunds() { return allowRefunds; }
        public void setAllowRefunds(boolean allowRefunds) { this.allowRefunds = allowRefunds; }
        
        public boolean isAllowPartialRefunds() { return allowPartialRefunds; }
        public void setAllowPartialRefunds(boolean allowPartialRefunds) { this.allowPartialRefunds = allowPartialRefunds; }
        
        public int getTokenExpiryDays() { return tokenExpiryDays; }
        public void setTokenExpiryDays(int tokenExpiryDays) { this.tokenExpiryDays = tokenExpiryDays; }
        
        public int getMaxTokensPerCard() { return maxTokensPerCard; }
        public void setMaxTokensPerCard(int maxTokensPerCard) { this.maxTokensPerCard = maxTokensPerCard; }
        
        public boolean isNotifyOnTokenCreation() { return notifyOnTokenCreation; }
        public void setNotifyOnTokenCreation(boolean notifyOnTokenCreation) { this.notifyOnTokenCreation = notifyOnTokenCreation; }
    }
    
    // Nested DTO for merchant statistics
    public static class MerchantStats {
        private long totalTokens;
        private long activeTokens;
        private long totalTransactions;
        private long tokensCreatedToday;
        
        // Getters and setters
        public long getTotalTokens() { return totalTokens; }
        public void setTotalTokens(long totalTokens) { this.totalTokens = totalTokens; }
        
        public long getActiveTokens() { return activeTokens; }
        public void setActiveTokens(long activeTokens) { this.activeTokens = activeTokens; }
        
        public long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public long getTokensCreatedToday() { return tokensCreatedToday; }
        public void setTokensCreatedToday(long tokensCreatedToday) { this.tokensCreatedToday = tokensCreatedToday; }
    }
    
    // Constructors
    public MerchantResponse() {}
    
    // Builder pattern for easy construction
    public static class Builder {
        private MerchantResponse response = new MerchantResponse();
        
        public Builder merchantId(String merchantId) {
            response.merchantId = merchantId;
            return this;
        }
        
        public Builder businessName(String businessName) {
            response.businessName = businessName;
            return this;
        }
        
        public Builder email(String email) {
            response.email = email;
            return this;
        }
        
        public Builder phoneNumber(String phoneNumber) {
            response.phoneNumber = phoneNumber;
            return this;
        }
        
        public Builder businessType(String businessType) {
            response.businessType = businessType;
            return this;
        }
        
        public Builder businessAddress(String businessAddress) {
            response.businessAddress = businessAddress;
            return this;
        }
        
        public Builder panNumber(String panNumber) {
            response.panNumber = panNumber;
            return this;
        }
        
        public Builder gstNumber(String gstNumber) {
            response.gstNumber = gstNumber;
            return this;
        }
        
        public Builder status(String status) {
            response.status = status;
            return this;
        }
        
        public Builder webhookUrl(String webhookUrl) {
            response.webhookUrl = webhookUrl;
            return this;
        }
        
        public Builder apiCredentials(ApiCredentials apiCredentials) {
            response.apiCredentials = apiCredentials;
            return this;
        }
        
        public Builder settings(MerchantSettings settings) {
            response.settings = settings;
            return this;
        }
        
        public Builder stats(MerchantStats stats) {
            response.stats = stats;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }
        
        public MerchantResponse build() {
            return response;
        }
    }
    
    // Getters and setters
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    
    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }
    
    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    
    public ApiCredentials getApiCredentials() { return apiCredentials; }
    public void setApiCredentials(ApiCredentials apiCredentials) { this.apiCredentials = apiCredentials; }
    
    public MerchantSettings getSettings() { return settings; }
    public void setSettings(MerchantSettings settings) { this.settings = settings; }
    
    public MerchantStats getStats() { return stats; }
    public void setStats(MerchantStats stats) { this.stats = stats; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}