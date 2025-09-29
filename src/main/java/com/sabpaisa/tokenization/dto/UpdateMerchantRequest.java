package com.sabpaisa.tokenization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateMerchantRequest {
    
    @Size(min = 3, max = 100, message = "Business name must be between 3 and 100 characters")
    private String businessName;
    
    @Email(message = "Email should be valid")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phoneNumber;
    
    private String businessType;
    
    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;
    
    private String webhookUrl;
    
    @Pattern(regexp = "^(ACTIVE|SUSPENDED|INACTIVE)$", message = "Status must be ACTIVE, SUSPENDED, or INACTIVE")
    private String status;
    
    private MerchantSettingsDto settings;
    
    // Nested DTO for merchant settings
    public static class MerchantSettingsDto {
        private Boolean allowRefunds;
        private Boolean allowPartialRefunds;
        private Integer tokenExpiryDays;
        private Integer maxTokensPerCard;
        private Boolean notifyOnTokenCreation;
        
        // Getters and setters
        public Boolean getAllowRefunds() { return allowRefunds; }
        public void setAllowRefunds(Boolean allowRefunds) { this.allowRefunds = allowRefunds; }
        
        public Boolean getAllowPartialRefunds() { return allowPartialRefunds; }
        public void setAllowPartialRefunds(Boolean allowPartialRefunds) { this.allowPartialRefunds = allowPartialRefunds; }
        
        public Integer getTokenExpiryDays() { return tokenExpiryDays; }
        public void setTokenExpiryDays(Integer tokenExpiryDays) { this.tokenExpiryDays = tokenExpiryDays; }
        
        public Integer getMaxTokensPerCard() { return maxTokensPerCard; }
        public void setMaxTokensPerCard(Integer maxTokensPerCard) { this.maxTokensPerCard = maxTokensPerCard; }
        
        public Boolean getNotifyOnTokenCreation() { return notifyOnTokenCreation; }
        public void setNotifyOnTokenCreation(Boolean notifyOnTokenCreation) { this.notifyOnTokenCreation = notifyOnTokenCreation; }
    }
    
    // Constructors
    public UpdateMerchantRequest() {}
    
    // Getters and setters
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
    
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public MerchantSettingsDto getSettings() { return settings; }
    public void setSettings(MerchantSettingsDto settings) { this.settings = settings; }
}