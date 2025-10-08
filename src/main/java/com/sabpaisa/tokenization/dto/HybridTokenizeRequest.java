package com.sabpaisa.tokenization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

public class HybridTokenizeRequest {
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")
    private String cardNumber;
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    private Map<String, Object> biometricData;
    
    private Map<String, Object> securityOptions;
    
    private String[] enabledModes = {"BIOMETRIC", "QUANTUM", "CLOUD_REPLICATED"};
    
    // Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public Map<String, Object> getBiometricData() {
        return biometricData;
    }
    
    public void setBiometricData(Map<String, Object> biometricData) {
        this.biometricData = biometricData;
    }
    
    public Map<String, Object> getSecurityOptions() {
        return securityOptions;
    }
    
    public void setSecurityOptions(Map<String, Object> securityOptions) {
        this.securityOptions = securityOptions;
    }
    
    public String[] getEnabledModes() {
        return enabledModes;
    }
    
    public void setEnabledModes(String[] enabledModes) {
        this.enabledModes = enabledModes;
    }
}