package com.sabpaisa.tokenization.presentation.dto;

import com.sabpaisa.tokenization.domain.entity.BillingRecord;
import lombok.Data;
import java.util.List;

@Data
public class BillingDashboardResponse {
    private TokenUsageStats currentMonthUsage;
    private BillingCalculation estimatedCharges;
    private List<BillingRecord> historicalBilling;
    private UsageTrends usageTrends;
}