package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;

@Data
public class TokenTypeInfo {
    private String typeCode;
    private String typeName;
    private String description;
    private Integer defaultExpiryDays;
    private Integer maxTokensPerCard;
}