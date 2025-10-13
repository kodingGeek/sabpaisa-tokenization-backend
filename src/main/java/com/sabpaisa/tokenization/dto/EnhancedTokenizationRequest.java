package com.sabpaisa.tokenization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class EnhancedTokenizationRequest {
    
    // Basic card information
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")
    private String cardNumber;
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Invalid expiry month")
    private String expiryMonth;
    
    @Pattern(regexp = "^[0-9]{2}$", message = "Invalid expiry year")
    private String expiryYear;
    
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV")
    private String cvv;
    
    @Size(max = 100, message = "Cardholder name too long")
    private String cardholderName;
    
    // Algorithm selection
    @Pattern(regexp = "^(SIMPLE|COF|FPE)$", message = "Invalid algorithm type")
    private String algorithmType = "SIMPLE";
    
    // Customer information
    @Size(max = 50, message = "Customer ID too long")
    private String customerId;
    
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email too long")
    private String customerEmail;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String customerPhone;
    
    // Transaction information
    @Size(max = 50, message = "Transaction ID too long")
    private String transactionId;
    
    private BigDecimal transactionAmount;
    
    @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid currency code")
    private String transactionCurrency = "INR";
    
    // COF (Card on File) specific fields
    private boolean isCof;
    
    @Size(max = 50, message = "COF contract ID too long")
    private String cofContractId;
    
    @Size(max = 50, message = "COF initial transaction ID too long")
    private String cofInitialTransactionId;
    
    // Security context
    @Size(max = 100, message = "Device ID too long")
    private String deviceId;
    
    @Size(max = 100, message = "Session ID too long")
    private String sessionId;
    
    // Additional metadata
    private Map<String, String> metadata;
}