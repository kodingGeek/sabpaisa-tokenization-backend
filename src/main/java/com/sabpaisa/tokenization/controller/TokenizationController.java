package com.sabpaisa.tokenization.controller;

import com.sabpaisa.tokenization.dto.TokenizeRequest;
import com.sabpaisa.tokenization.dto.TokenResponse;
import com.sabpaisa.tokenization.dto.DetokenizeRequest;
import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.service.TokenizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/tokens")
@CrossOrigin(origins = "*") // Configure properly in production
@Tag(name = "Tokenization", description = "Tokenization API endpoints")
public class TokenizationController {
    
    private final TokenizationService tokenizationService;
    
    @Autowired
    public TokenizationController(TokenizationService tokenizationService) {
        this.tokenizationService = tokenizationService;
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
}