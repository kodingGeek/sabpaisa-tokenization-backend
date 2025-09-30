package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BulkRetokenizationRequest {
    
    private SelectionCriteria selectionCriteria;
    
    // For EXPIRING_SOON criteria
    private Integer daysBeforeExpiry;
    
    // For SPECIFIC_PLATFORM criteria
    private Long platformId;
    
    // For SPECIFIC_TOKENS criteria
    private List<Long> tokenIds;
    
    // For DATE_RANGE criteria
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // New token configuration
    private Integer newExpiryMonths;
    private boolean sendNotification = true;
    
    public enum SelectionCriteria {
        EXPIRED,
        EXPIRING_SOON,
        SPECIFIC_PLATFORM,
        SPECIFIC_TOKENS,
        DATE_RANGE
    }
}