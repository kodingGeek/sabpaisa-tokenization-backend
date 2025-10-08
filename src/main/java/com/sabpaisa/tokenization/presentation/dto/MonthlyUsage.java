package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;
import java.time.YearMonth;

@Data
public class MonthlyUsage {
    private YearMonth month;
    private long tokensCreated;
    private long transactions;
    private long activeTokens;
}