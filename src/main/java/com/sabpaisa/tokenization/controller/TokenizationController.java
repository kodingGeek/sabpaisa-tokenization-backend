package com.sabpaisa.tokenization.controller;

import com.sabpaisa.tokenization.dto.TokenizeRequest;
import com.sabpaisa.tokenization.dto.TokenResponse;
import com.sabpaisa.tokenization.dto.DetokenizeRequest;
import com.sabpaisa.tokenization.dto.TokenListResponse;
import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.entity.Merchant;
import com.sabpaisa.tokenization.service.TokenizationService;
import com.sabpaisa.tokenization.repository.MerchantRepository;
import com.sabpaisa.tokenization.repository.TokenRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tokens")
@CrossOrigin(origins = "*") // Configure properly in production
@Tag(name = "Tokenization", description = "Tokenization API endpoints")
public class TokenizationController {
    
    private final TokenizationService tokenizationService;
    private final MerchantRepository merchantRepository;
    private final TokenRepository tokenRepository;
    
    @Autowired
    public TokenizationController(TokenizationService tokenizationService,
                                  MerchantRepository merchantRepository,
                                  TokenRepository tokenRepository) {
        this.tokenizationService = tokenizationService;
        this.merchantRepository = merchantRepository;
        this.tokenRepository = tokenRepository;
    }
    
    @PostMapping("/tokenize")
    @Operation(summary = "Tokenize a card", description = "Convert a card number into a secure token")
    public ResponseEntity<TokenResponse> tokenize(@Valid @RequestBody TokenizeRequest request) {
        try {
            Token token = tokenizationService.tokenizeCard(
                request.getCardNumber(), 
                request.getMerchantId()
            );
            
            TokenResponse response = TokenResponse.success(
                token.getTokenValue(),
                token.getMaskedPan(),
                token.getStatus(),
                token.getExpiresAt()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            TokenResponse errorResponse = TokenResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/detokenize")
    @Operation(summary = "Detokenize a token", description = "Retrieve the masked card information for a token")
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
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            TokenResponse errorResponse = TokenResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Tokenization service is running");
    }
    
    @GetMapping
    @Operation(summary = "Get all tokens", description = "Retrieve all tokens with pagination and optional merchant filtering")
    public ResponseEntity<TokenListResponse> getAllTokens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String merchantId) {
        
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            TokenListResponse response = tokenizationService.getAllTokens(pageable, merchantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TokenListResponse());
        }
    }
    
    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "Get all tokens for a merchant", description = "Retrieve all tokens associated with a merchant")
    public ResponseEntity<List<TokenResponse>> getTokensByMerchant(@PathVariable String merchantId) {
        try {
            // Find merchant
            Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));
            
            // Get all tokens for merchant
            List<Token> tokens = tokenRepository.findByMerchant(merchant);
            
            // Convert to response DTOs
            List<TokenResponse> responses = tokens.stream()
                .map(token -> TokenResponse.success(
                    token.getTokenValue(),
                    token.getMaskedPan(),
                    token.getStatus(),
                    token.getExpiresAt()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @GetMapping("/merchant/{merchantId}/status/{status}")
    @Operation(summary = "Get tokens by status", description = "Retrieve all tokens for a merchant with specific status")
    public ResponseEntity<List<TokenResponse>> getTokensByMerchantAndStatus(
            @PathVariable String merchantId,
            @PathVariable String status) {
        try {
            // Find merchant
            Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));
            
            // Get tokens by status
            List<Token> tokens = tokenRepository.findByMerchantAndStatus(merchant, status);
            
            // Convert to response DTOs
            List<TokenResponse> responses = tokens.stream()
                .map(token -> TokenResponse.success(
                    token.getTokenValue(),
                    token.getMaskedPan(),
                    token.getStatus(),
                    token.getExpiresAt()
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}