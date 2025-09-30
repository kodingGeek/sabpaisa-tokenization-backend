package com.sabpaisa.tokenization.presentation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlatformTokenizationRequest {
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{13,19}$")
    private String cardNumber;
    
    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$")
    private String expiryDate; // MM/YY
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{3,4}$")
    private String cvv;
    
    @NotNull
    private Long platformId;
    
    @NotBlank
    private String tokenTypeCode; // COF, FPT, etc.
    
    private String customerEmail;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$")
    private String customerPhone;
    
    private String customerId;
    
    private boolean enableNotifications = true;
    
    @Min(7)
    @Max(90)
    private Integer daysBeforeExpiryNotification = 30;
    
    private Integer customExpiryMonths; // Override default token type expiry
}