package com.sabpaisa.tokenization.presentation.controller;

import com.sabpaisa.tokenization.domain.entity.EnhancedToken;
import com.sabpaisa.tokenization.domain.entity.Platform;
import com.sabpaisa.tokenization.presentation.dto.*;
import com.sabpaisa.tokenization.service.PlatformTokenizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@RestController
@RequestMapping("/api/v1/platform-tokens")
@RequiredArgsConstructor
public class PlatformTokenizationController {
    
    private final PlatformTokenizationService platformTokenizationService;
    
    @PostMapping("/tokenize")
    public ResponseEntity<PlatformTokenizationResponse> tokenizeForPlatform(
            @Valid @RequestBody PlatformTokenizationRequest request,
            @RequestHeader("X-Merchant-Id") String merchantId) {
        
        EnhancedToken token = platformTokenizationService.createPlatformToken(request, merchantId);
        
        PlatformTokenizationResponse response = new PlatformTokenizationResponse();
        response.setTokenId(token.getId());
        response.setTokenValue(token.getTokenValue());
        response.setPlatformCode(token.getPlatform().getPlatformCode());
        response.setPlatformName(token.getPlatform().getPlatformName());
        response.setTokenType(token.getTokenType().getTypeCode());
        response.setExpiryDate(token.getExpiryDate());
        response.setMaskedPan(token.getMaskedPan());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformInfo>> getAvailablePlatforms(
            @RequestHeader("X-Merchant-Id") String merchantId) {
        
        List<PlatformInfo> platforms = platformTokenizationService.getMerchantPlatforms(merchantId);
        return ResponseEntity.ok(platforms);
    }
    
    @GetMapping("/token-types")
    public ResponseEntity<List<TokenTypeInfo>> getTokenTypes() {
        List<TokenTypeInfo> tokenTypes = platformTokenizationService.getActiveTokenTypes();
        return ResponseEntity.ok(tokenTypes);
    }
    
    @GetMapping("/card/{cardHash}")
    public ResponseEntity<List<CardTokenInfo>> getTokensForCard(
            @PathVariable String cardHash,
            @RequestHeader("X-Merchant-Id") String merchantId) {
        
        List<CardTokenInfo> tokens = platformTokenizationService.getTokensForCard(cardHash, merchantId);
        return ResponseEntity.ok(tokens);
    }
    
    @PostMapping("/platforms")
    public ResponseEntity<Platform> createPlatform(
            @Valid @RequestBody CreatePlatformRequest request,
            @RequestHeader("X-Merchant-Id") String merchantId) {
        
        Platform platform = platformTokenizationService.createPlatform(request, merchantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(platform);
    }
}