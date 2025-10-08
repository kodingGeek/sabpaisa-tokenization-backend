package com.sabpaisa.tokenization.presentation.controller;

import com.sabpaisa.tokenization.presentation.dto.BulkRetokenizationRequest;
import com.sabpaisa.tokenization.presentation.dto.BulkRetokenizationResponse;
import com.sabpaisa.tokenization.service.BulkRetokenizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/tokens/bulk")
@RequiredArgsConstructor
public class BulkRetokenizationController {
    
    private final BulkRetokenizationService bulkRetokenizationService;
    
    @PostMapping("/retokenize")
    public ResponseEntity<BulkRetokenizationResponse> bulkRetokenize(
            @Valid @RequestBody BulkRetokenizationRequest request,
            @RequestHeader("X-Merchant-Id") String merchantId) {
        
        CompletableFuture<BulkRetokenizationResponse> future = 
            bulkRetokenizationService.processBulkRetokenization(request, merchantId);
        
        // For async processing, return immediately with request ID
        BulkRetokenizationResponse response = new BulkRetokenizationResponse();
        response.setStatus("PROCESSING");
        response.setRequestId("BULK_" + System.currentTimeMillis());
        
        return ResponseEntity.accepted().body(response);
    }
    
    @GetMapping("/status/{requestId}")
    public ResponseEntity<BulkRetokenizationResponse> getBulkStatus(
            @PathVariable String requestId,
            @RequestHeader("X-Merchant-Id") String merchantId) {
        
        // In production, retrieve status from persistent store
        BulkRetokenizationResponse response = new BulkRetokenizationResponse();
        response.setRequestId(requestId);
        response.setStatus("COMPLETED");
        
        return ResponseEntity.ok(response);
    }
}