package com.sabpaisa.tokenization.controller;

import com.sabpaisa.tokenization.dto.*;
import com.sabpaisa.tokenization.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
@CrossOrigin(origins = "*") // Configure properly in production
@Tag(name = "Merchant Management", description = "Merchant management endpoints")
public class MerchantController {
    
    private final MerchantService merchantService;
    
    @Autowired
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new merchant", description = "Register a new merchant in the system")
    public ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        try {
            MerchantResponse response = merchantService.createMerchant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @GetMapping("/{merchantId}")
    @Operation(summary = "Get merchant by ID", description = "Retrieve merchant details by merchant ID")
    public ResponseEntity<MerchantResponse> getMerchant(@PathVariable String merchantId) {
        try {
            MerchantResponse response = merchantService.getMerchantById(merchantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    @PutMapping("/{merchantId}")
    @Operation(summary = "Update merchant", description = "Update merchant details")
    public ResponseEntity<MerchantResponse> updateMerchant(
            @PathVariable String merchantId,
            @Valid @RequestBody UpdateMerchantRequest request) {
        try {
            MerchantResponse response = merchantService.updateMerchant(merchantId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all merchants", description = "Retrieve all merchants with pagination")
    public ResponseEntity<MerchantListResponse> getAllMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String status) {
        
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            MerchantListResponse response = merchantService.getAllMerchants(pageable, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @DeleteMapping("/{merchantId}")
    @Operation(summary = "Delete merchant", description = "Soft delete a merchant by setting status to INACTIVE")
    public ResponseEntity<Void> deleteMerchant(@PathVariable String merchantId) {
        try {
            merchantService.deleteMerchant(merchantId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @PostMapping("/{merchantId}/regenerate-credentials")
    @Operation(summary = "Regenerate API credentials", description = "Generate new API key and secret for a merchant")
    public ResponseEntity<MerchantResponse> regenerateCredentials(@PathVariable String merchantId) {
        try {
            MerchantResponse response = merchantService.regenerateApiCredentials(merchantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search merchants", description = "Search merchants by name or email")
    public ResponseEntity<MerchantListResponse> searchMerchants(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // TODO: Implement search functionality
        return ResponseEntity.ok(new MerchantListResponse());
    }
    
    @GetMapping("/business-types")
    @Operation(summary = "Get business types", description = "Get list of supported business types")
    public ResponseEntity<String[]> getBusinessTypes() {
        String[] types = {"RETAIL", "ECOMMERCE", "SERVICES", "HOSPITALITY", "HEALTHCARE", "EDUCATION", "OTHER"};
        return ResponseEntity.ok(types);
    }
}