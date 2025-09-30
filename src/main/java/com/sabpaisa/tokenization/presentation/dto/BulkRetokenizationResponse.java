package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BulkRetokenizationResponse {
    private String requestId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private String errorMessage;
    private List<RetokenizationResult> results;
}