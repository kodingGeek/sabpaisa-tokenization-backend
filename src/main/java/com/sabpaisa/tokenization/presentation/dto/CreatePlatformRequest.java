package com.sabpaisa.tokenization.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePlatformRequest {
    
    @NotBlank
    @Pattern(regexp = "^[A-Z0-9_]{3,20}$", message = "Platform code must be 3-20 uppercase alphanumeric characters")
    private String platformCode;
    
    @NotBlank
    private String platformName;
    
    private String description;
    
    private String iconUrl;
    
    private String webhookUrl;
    
    private String allowedDomains;
}