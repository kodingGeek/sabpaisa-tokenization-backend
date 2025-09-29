package com.sabpaisa.tokenization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateMerchantRequest {
    
    @NotBlank(message = "Business name is required")
    @Size(min = 3, max = 100, message = "Business name must be between 3 and 100 characters")
    private String businessName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phoneNumber;
    
    @NotBlank(message = "Business type is required")
    private String businessType; // RETAIL, ECOMMERCE, SERVICES, etc.
    
    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;
    
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN number format")
    private String panNumber;
    
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "Invalid GST number format")
    private String gstNumber;
    
    private String webhookUrl;
    
    private MerchantSettingsDto settings;
    
    // Nested DTO for merchant settings
    public static class MerchantSettingsDto {
        private boolean allowRefunds = true;
        private boolean allowPartialRefunds = false;
        private Integer tokenExpiryDays = 1095; // 3 years default
        private Integer maxTokensPerCard = 5;
        private boolean notifyOnTokenCreation = true;
        
        // Getters and setters
        public boolean isAllowRefunds() { return allowRefunds; }
        public void setAllowRefunds(boolean allowRefunds) { this.allowRefunds = allowRefunds; }
        
        public boolean isAllowPartialRefunds() { return allowPartialRefunds; }
        public void setAllowPartialRefunds(boolean allowPartialRefunds) { this.allowPartialRefunds = allowPartialRefunds; }
        
        public Integer getTokenExpiryDays() { return tokenExpiryDays; }
        public void setTokenExpiryDays(Integer tokenExpiryDays) { this.tokenExpiryDays = tokenExpiryDays; }
        
        public Integer getMaxTokensPerCard() { return maxTokensPerCard; }
        public void setMaxTokensPerCard(Integer maxTokensPerCard) { this.maxTokensPerCard = maxTokensPerCard; }
        
        public boolean isNotifyOnTokenCreation() { return notifyOnTokenCreation; }
        public void setNotifyOnTokenCreation(boolean notifyOnTokenCreation) { this.notifyOnTokenCreation = notifyOnTokenCreation; }
    }
    
    // Constructors
    public CreateMerchantRequest() {}
    
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
    
    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    
    public MerchantSettingsDto getSettings() { return settings; }
    public void setSettings(MerchantSettingsDto settings) { this.settings = settings; }
}