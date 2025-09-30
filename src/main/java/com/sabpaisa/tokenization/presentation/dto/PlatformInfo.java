package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;

@Data
public class PlatformInfo {
    private Long id;
    private String platformCode;
    private String platformName;
    private String description;
    private String iconUrl;
}