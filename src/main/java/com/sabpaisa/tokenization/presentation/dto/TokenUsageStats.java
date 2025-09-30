package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TokenUsageStats {
    private long totalTokensCreated;
    private long totalActiveTokens;
    private long totalTransactions;
    private Map<String, Long> platformBreakdown;
}