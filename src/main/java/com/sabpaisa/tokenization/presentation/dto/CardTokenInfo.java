package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CardTokenInfo {
    private Long tokenId;
    private String tokenValue;
    private String platformName;
    private String tokenType;
    private LocalDateTime expiryDate;
    private Boolean isActive;
}