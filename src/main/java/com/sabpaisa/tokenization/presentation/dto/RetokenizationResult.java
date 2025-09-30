package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;

@Data
public class RetokenizationResult {
    private Long oldTokenId;
    private String oldTokenValue;
    private Long newTokenId;
    private String newTokenValue;
    private String cardLast4;
    private boolean success;
    private String message;
}