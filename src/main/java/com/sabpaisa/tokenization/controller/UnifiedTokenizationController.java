package com.sabpaisa.tokenization.controller;

import com.sabpaisa.tokenization.dto.*;
import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.service.UnifiedTokenizationService;
import com.sabpaisa.tokenization.service.UnifiedTokenizationService.TokenizationMode;
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

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified Tokenization Controller
 * 
 * Provides REST API endpoints for all tokenization modes:
 * - Standard tokenization (PCI DSS compliant)
 * - Biometric-enhanced tokenization
 * - Quantum-resistant tokenization
 * - Cloud-replicated tokenization
 * - Hybrid multi-mode tokenization
 */
@RestController
@RequestMapping("/api/v1/unified-tokens")
@CrossOrigin(origins = "*")
@Tag(name = "Unified Tokenization", description = "Unified tokenization API with multiple security modes")
public class UnifiedTokenizationController {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedTokenizationController.class);
    
    @Autowired
    private UnifiedTokenizationService tokenizationService;
    
    @PostMapping("/tokenize")
    @Operation(summary = "Tokenize with specified mode", 
              description = "Create token using specified security mode (standard, biometric, quantum, cloud_replicated, hybrid)")
    public ResponseEntity<TokenResponse> tokenizeWithMode(
            @Valid @RequestBody UnifiedTokenizeRequest request,
            @RequestHeader Map<String, String> headers) {
        
        try {
            // Prepare options
            Map<String, Object> options = new HashMap<>();
            options.put("headers", headers);
            
            if (request.getBiometricData() != null) {
                options.put("biometricData", request.getBiometricData());
            }
            
            if (request.getSecurityOptions() != null) {
                options.putAll(request.getSecurityOptions());
            }
            
            // Determine tokenization mode
            TokenizationMode mode = TokenizationMode.valueOf(
                request.getTokenizationMode() != null ? 
                request.getTokenizationMode().toUpperCase() : "STANDARD"
            );
            
            // Tokenize
            Token token = tokenizationService.tokenize(
                request.getCardNumber(),
                request.getMerchantId(),
                mode,
                options
            );
            
            // Build response
            TokenResponse response = TokenResponse.success(
                token.getTokenValue(),
                token.getMaskedPan(),
                token.getStatus(),
                token.getExpiresAt()
            );
            
            // Add mode and metadata to response
            response.setTokenizationMode(token.getAlgorithmType());
            response.setMetadata(token.getMetadata());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Tokenization failed", e);
            TokenResponse errorResponse = TokenResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/tokenize/standard")
    @Operation(summary = "Standard tokenization", description = "Create token using standard mode (backward compatible)")
    public ResponseEntity<TokenResponse> tokenizeStandard(
            @Valid @RequestBody TokenizeRequest request,
            @RequestHeader(required = false) Map<String, String> headers) {
        
        UnifiedTokenizeRequest unifiedRequest = new UnifiedTokenizeRequest();
        unifiedRequest.setCardNumber(request.getCardNumber());
        unifiedRequest.setMerchantId(request.getMerchantId());
        unifiedRequest.setTokenizationMode("STANDARD");
        
        return tokenizeWithMode(unifiedRequest, headers != null ? headers : new HashMap<>());
    }
    
    @PostMapping("/tokenize/biometric")
    @Operation(summary = "Biometric tokenization", description = "Create token with biometric protection")
    public ResponseEntity<TokenResponse> tokenizeBiometric(
            @Valid @RequestBody BiometricTokenizeRequest request,
            @RequestHeader Map<String, String> headers) {
        
        UnifiedTokenizeRequest unifiedRequest = new UnifiedTokenizeRequest();
        unifiedRequest.setCardNumber(request.getCardNumber());
        unifiedRequest.setMerchantId(request.getMerchantId());
        unifiedRequest.setTokenizationMode("BIOMETRIC");
        unifiedRequest.setBiometricData(request.getBiometricData());
        
        return tokenizeWithMode(unifiedRequest, headers);
    }
    
    @PostMapping("/tokenize/quantum")
    @Operation(summary = "Quantum-resistant tokenization", description = "Create token with quantum-resistant encryption")
    public ResponseEntity<TokenResponse> tokenizeQuantum(
            @Valid @RequestBody TokenizeRequest request,
            @RequestHeader Map<String, String> headers) {
        
        UnifiedTokenizeRequest unifiedRequest = new UnifiedTokenizeRequest();
        unifiedRequest.setCardNumber(request.getCardNumber());
        unifiedRequest.setMerchantId(request.getMerchantId());
        unifiedRequest.setTokenizationMode("QUANTUM");
        
        return tokenizeWithMode(unifiedRequest, headers);
    }
    
    @PostMapping("/tokenize/cloud")
    @Operation(summary = "Cloud-replicated tokenization", description = "Create token with multi-cloud replication")
    public ResponseEntity<TokenResponse> tokenizeCloudReplicated(
            @Valid @RequestBody TokenizeRequest request,
            @RequestHeader Map<String, String> headers) {
        
        UnifiedTokenizeRequest unifiedRequest = new UnifiedTokenizeRequest();
        unifiedRequest.setCardNumber(request.getCardNumber());
        unifiedRequest.setMerchantId(request.getMerchantId());
        unifiedRequest.setTokenizationMode("CLOUD_REPLICATED");
        
        return tokenizeWithMode(unifiedRequest, headers);
    }
    
    @PostMapping("/tokenize/hybrid")
    @Operation(summary = "Hybrid tokenization", description = "Create token with multiple security layers")
    public ResponseEntity<TokenResponse> tokenizeHybrid(
            @Valid @RequestBody HybridTokenizeRequest request,
            @RequestHeader Map<String, String> headers) {
        
        UnifiedTokenizeRequest unifiedRequest = new UnifiedTokenizeRequest();
        unifiedRequest.setCardNumber(request.getCardNumber());
        unifiedRequest.setMerchantId(request.getMerchantId());
        unifiedRequest.setTokenizationMode("HYBRID");
        unifiedRequest.setBiometricData(request.getBiometricData());
        unifiedRequest.setSecurityOptions(request.getSecurityOptions());
        
        return tokenizeWithMode(unifiedRequest, headers);
    }
    
    @PostMapping("/detokenize")
    @Operation(summary = "Detokenize", description = "Retrieve masked card information from token")
    public ResponseEntity<TokenResponse> detokenize(@Valid @RequestBody DetokenizeRequest request) {
        try {
            Token token = tokenizationService.detokenize(
                request.getToken(),
                request.getMerchantId()
            );
            
            TokenResponse response = TokenResponse.success(
                token.getTokenValue(),
                token.getMaskedPan(),
                token.getStatus(),
                token.getExpiresAt()
            );
            
            response.setTokenizationMode(token.getAlgorithmType());
            response.setUsageCount(token.getUsageCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Detokenization failed", e);
            TokenResponse errorResponse = TokenResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping
    @Operation(summary = "List all tokens", description = "Get paginated list of tokens with optional filtering")
    public ResponseEntity<TokenListResponse> listTokens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String mode) {
        
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            TokenListResponse response = tokenizationService.getAllTokens(pageable, merchantId);
            
            // Filter by mode if specified
            if (mode != null && !mode.isEmpty()) {
                response.getTokens().removeIf(token -> 
                    !mode.equalsIgnoreCase(token.getTokenizationMode())
                );
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to list tokens", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TokenListResponse());
        }
    }
    
    @GetMapping("/modes")
    @Operation(summary = "Get available tokenization modes", description = "List all supported tokenization modes")
    public ResponseEntity<Map<String, Object>> getTokenizationModes() {
        Map<String, Object> modes = new HashMap<>();
        
        modes.put("STANDARD", Map.of(
            "name", "Standard Tokenization",
            "description", "PCI DSS compliant tokenization with fraud detection",
            "features", new String[]{"16-digit numeric tokens", "Card masking", "Fraud detection", "3-year expiry"}
        ));
        
        modes.put("BIOMETRIC", Map.of(
            "name", "Biometric-Enhanced Tokenization",
            "description", "Tokenization with biometric authentication",
            "features", new String[]{"Facial recognition", "Fingerprint matching", "Voice biometrics", "Liveness detection"}
        ));
        
        modes.put("QUANTUM", Map.of(
            "name", "Quantum-Resistant Tokenization",
            "description", "Post-quantum cryptographic protection",
            "features", new String[]{"NIST Level 5 security", "Quantum key rotation", "Quantum-safe algorithms", "Future-proof encryption"}
        ));
        
        modes.put("CLOUD_REPLICATED", Map.of(
            "name", "Cloud-Replicated Tokenization",
            "description", "Multi-cloud distributed storage",
            "features", new String[]{"AWS S3 replication", "Azure Blob storage", "GCP Cloud Storage", "Automatic failover"}
        ));
        
        modes.put("HYBRID", Map.of(
            "name", "Hybrid Multi-Mode Tokenization",
            "description", "Combines multiple security modes",
            "features", new String[]{"Customizable security layers", "Best-of-breed protection", "Maximum security", "Flexible configuration"}
        ));
        
        return ResponseEntity.ok(modes);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check service health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Unified Tokenization Service is running");
    }
}