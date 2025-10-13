package com.sabpaisa.tokenization.algorithm;

import com.sabpaisa.tokenization.dto.TokenizationContext;

/**
 * Interface for tokenization algorithms
 */
public interface TokenizationAlgorithm {
    
    /**
     * Generate a token for the given card number
     * 
     * @param context The tokenization context containing all necessary information
     * @return The generated token value
     */
    String tokenize(TokenizationContext context);
    
    /**
     * Validate if a token is valid according to the algorithm rules
     * 
     * @param token The token to validate
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token);
    
    /**
     * Get the algorithm type identifier
     * 
     * @return The algorithm type (SIMPLE, COF, FPE)
     */
    String getAlgorithmType();
    
    /**
     * Get the token format for this algorithm
     * 
     * @return The token format (NUMERIC, ALPHANUMERIC, PRESERVE_FORMAT)
     */
    String getTokenFormat();
}