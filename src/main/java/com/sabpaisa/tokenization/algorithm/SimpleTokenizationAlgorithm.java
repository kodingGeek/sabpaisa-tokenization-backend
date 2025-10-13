package com.sabpaisa.tokenization.algorithm;

import com.sabpaisa.tokenization.dto.TokenizationContext;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Simple tokenization algorithm that generates random numeric tokens
 */
@Component
public class SimpleTokenizationAlgorithm implements TokenizationAlgorithm {
    
    private static final int TOKEN_LENGTH = 16;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Override
    public String tokenize(TokenizationContext context) {
        // Generate a random 16-digit numeric token
        StringBuilder token = new StringBuilder();
        
        // Ensure first digit is not 0
        token.append(1 + secureRandom.nextInt(9));
        
        // Generate remaining 15 digits
        for (int i = 1; i < TOKEN_LENGTH; i++) {
            token.append(secureRandom.nextInt(10));
        }
        
        return token.toString();
    }
    
    @Override
    public boolean validateToken(String token) {
        if (token == null || token.length() != TOKEN_LENGTH) {
            return false;
        }
        
        // Check if all characters are digits
        return token.matches("\\d{" + TOKEN_LENGTH + "}");
    }
    
    @Override
    public String getAlgorithmType() {
        return "SIMPLE";
    }
    
    @Override
    public String getTokenFormat() {
        return "NUMERIC";
    }
}