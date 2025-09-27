package com.sabpaisa.tokenization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TokenizeRequest {
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9\\s]{13,19}$", message = "Invalid card number format")
    private String cardNumber;
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    // Constructors
    public TokenizeRequest() {}
    
    public TokenizeRequest(String cardNumber, String merchantId) {
        this.cardNumber = cardNumber;
        this.merchantId = merchantId;
    }
    
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
}