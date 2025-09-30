package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;
import java.util.List;

@Data
public class UsageTrends {
    private List<MonthlyUsage> monthlyUsages;
    private double tokenCreationGrowthRate;
    private double transactionGrowthRate;
}