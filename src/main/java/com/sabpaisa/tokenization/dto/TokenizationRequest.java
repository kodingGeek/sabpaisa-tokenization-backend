package com.sabpaisa.tokenization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TokenizationRequest {
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")
    private String cardNumber;
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    private String cardholderName;
    private String expiryMonth;
    private String expiryYear;
    
    public TokenizationRequest() {}
    
    public TokenizationRequest(String cardNumber, String merchantId) {
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
    
    public String getCardholderName() {
        return cardholderName;
    }
    
    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }
    
    public String getExpiryMonth() {
        return expiryMonth;
    }
    
    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }
    
    public String getExpiryYear() {
        return expiryYear;
    }
    
    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }
}