package com.sabpaisa.tokenization.presentation.controller;

import com.sabpaisa.tokenization.presentation.dto.BillingDashboardResponse;
import com.sabpaisa.tokenization.service.TokenMonetizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class TokenMonetizationController {
    
    private final TokenMonetizationService monetizationService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<BillingDashboardResponse> getBillingDashboard(
            @RequestHeader("X-Merchant-Id") String merchantId) {
        
        BillingDashboardResponse dashboard = monetizationService.getMerchantBillingDashboard(merchantId);
        return ResponseEntity.ok(dashboard);
    }
    
    @PostMapping("/generate-invoice/{billingRecordId}")
    public ResponseEntity<String> generateInvoice(
            @PathVariable Long billingRecordId,
            @RequestHeader("X-Merchant-Id") String merchantId) {
        
        // Generate invoice PDF
        return ResponseEntity.ok("Invoice generation initiated");
    }
}