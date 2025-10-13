package com.sabpaisa.tokenization.controller;

import com.sabpaisa.tokenization.dto.ApiResponse;
import com.sabpaisa.tokenization.dto.EnhancedTokenizationRequest;
import com.sabpaisa.tokenization.dto.TokenResponse;
import com.sabpaisa.tokenization.dto.TokenizationRequest;
import com.sabpaisa.tokenization.service.EnhancedTokenizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/tokens")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enhanced Token Management", description = "Comprehensive token management APIs with multiple algorithms")
@SecurityRequirement(name = "bearer-key")
public class EnhancedTokenController {
    
    private final EnhancedTokenizationService tokenizationService;
    
    @PostMapping
    @Operation(summary = "Create a new token", description = "Create a token using specified algorithm (SIMPLE, COF, FPE)")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>> createToken(
            @Valid @RequestBody EnhancedTokenizationRequest request,
            HttpServletRequest httpRequest) {
        try {
            TokenResponse response = tokenizationService.createToken(request, httpRequest);
            return ResponseEntity.ok(ApiResponse.success("Token created successfully", response));
        } catch (SecurityException e) {
            log.error("Security error creating token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Security violation: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for token creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create token"));
        }
    }
    
    @GetMapping("/{tokenValue}")
    @Operation(summary = "Get token details", description = "Retrieve detailed information about a specific token")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>> getToken(
            @PathVariable String tokenValue,
            @RequestParam String merchantId) {
        try {
            TokenResponse response = tokenizationService.getToken(tokenValue, merchantId);
            return ResponseEntity.ok(ApiResponse.success("Token retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            log.error("Token not found: {}", tokenValue);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Token not found"));
        } catch (Exception e) {
            log.error("Error retrieving token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve token"));
        }
    }
    
    @PutMapping("/{tokenValue}/status")
    @Operation(summary = "Update token status", description = "Update the status of a token (ACTIVE, SUSPENDED, REVOKED)")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>> updateTokenStatus(
            @PathVariable String tokenValue,
            @RequestParam String merchantId,
            @RequestParam @Parameter(description = "New status: ACTIVE, SUSPENDED, or REVOKED") String status) {
        try {
            // Validate status
            if (!status.matches("^(ACTIVE|SUSPENDED|REVOKED)$")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status. Must be ACTIVE, SUSPENDED, or REVOKED"));
            }
            
            TokenResponse response = tokenizationService.updateTokenStatus(tokenValue, status, merchantId);
            return ResponseEntity.ok(ApiResponse.success("Token status updated successfully", response));
        } catch (IllegalArgumentException e) {
            log.error("Error updating token status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating token status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update token status"));
        }
    }
    
    @DeleteMapping("/{tokenValue}")
    @Operation(summary = "Delete a token", description = "Soft delete a token by revoking it")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteToken(
            @PathVariable String tokenValue,
            @RequestParam String merchantId) {
        try {
            tokenizationService.deleteToken(tokenValue, merchantId);
            return ResponseEntity.ok(ApiResponse.success("Token deleted successfully", null));
        } catch (IllegalArgumentException e) {
            log.error("Token not found for deletion: {}", tokenValue);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Token not found"));
        } catch (Exception e) {
            log.error("Error deleting token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete token"));
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search tokens", description = "Search tokens with various filters")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<TokenResponse>>> searchTokens(
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) @Parameter(description = "Token status") String status,
            @RequestParam(required = false) @Parameter(description = "Algorithm type: SIMPLE, COF, or FPE") String algorithmType,
            @RequestParam(required = false) @Parameter(description = "Card brand: VISA, MASTERCARD, etc.") String cardBrand,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<TokenResponse> tokens = tokenizationService.searchTokens(
                merchantId, status, algorithmType, cardBrand, fromDate, toDate, pageable);
            return ResponseEntity.ok(ApiResponse.success("Tokens retrieved successfully", tokens));
        } catch (Exception e) {
            log.error("Error searching tokens", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search tokens"));
        }
    }
    
    @GetMapping("/algorithms")
    @Operation(summary = "Get available algorithms", description = "Get list of available tokenization algorithms")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAlgorithms() {
        Map<String, String> algorithms = Map.of(
            "SIMPLE", "Simple random numeric token generation",
            "COF", "Card-on-File deterministic tokenization for recurring payments",
            "FPE", "Format Preserving Encryption that maintains card number format"
        );
        return ResponseEntity.ok(ApiResponse.success("Available algorithms", algorithms));
    }
    
    @GetMapping("/validate/{tokenValue}")
    @Operation(summary = "Validate token", description = "Check if a token is valid and active")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @PathVariable String tokenValue,
            @RequestParam String merchantId) {
        try {
            TokenResponse token = tokenizationService.getToken(tokenValue, merchantId);
            
            Map<String, Object> validation = Map.of(
                "isValid", true,
                "status", token.getStatus(),
                "isActive", "ACTIVE".equals(token.getStatus()),
                "expiresAt", token.getExpiresAt(),
                "algorithmType", token.getAlgorithmType()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Token validation result", validation));
        } catch (IllegalArgumentException e) {
            Map<String, Object> validation = Map.of(
                "isValid", false,
                "error", "Token not found"
            );
            return ResponseEntity.ok(ApiResponse.success("Token validation result", validation));
        } catch (Exception e) {
            log.error("Error validating token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to validate token"));
        }
    }
}