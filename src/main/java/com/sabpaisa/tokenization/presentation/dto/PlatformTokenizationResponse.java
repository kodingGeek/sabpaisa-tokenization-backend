package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlatformTokenizationResponse {
    private Long tokenId;
    private String tokenValue;
    private String platformCode;
    private String platformName;
    private String tokenType;
    private LocalDateTime expiryDate;
    private String maskedPan;
    private String cardBrand;
    private String cardType;
}