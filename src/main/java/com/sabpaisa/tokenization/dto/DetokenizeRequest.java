package com.sabpaisa.tokenization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DetokenizeRequest {
    
    @NotBlank(message = "Token is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Invalid token format")
    private String token;
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    // Constructors
    public DetokenizeRequest() {}
    
    public DetokenizeRequest(String token, String merchantId) {
        this.token = token;
        this.merchantId = merchantId;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}