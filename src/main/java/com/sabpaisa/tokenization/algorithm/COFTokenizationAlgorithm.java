package com.sabpaisa.tokenization.algorithm;

import com.sabpaisa.tokenization.dto.TokenizationContext;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Card-on-File (COF) tokenization algorithm that generates deterministic tokens
 * for recurring transactions using HMAC-based approach
 */
@Component
public class COFTokenizationAlgorithm implements TokenizationAlgorithm {
    
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int TOKEN_LENGTH = 16;
    private static final String COF_PREFIX = "COF";
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Override
    public String tokenize(TokenizationContext context) {
        if (!context.isCof() || context.getCofContractId() == null) {
            throw new IllegalArgumentException("COF tokenization requires contract ID");
        }
        
        try {
            // Create a deterministic seed based on card number, merchant ID, and contract ID
            String seed = context.getCardNumber() + ":" + 
                         context.getMerchantId() + ":" + 
                         context.getCofContractId();
            
            // Generate HMAC using a secret key (in production, this would be from secure storage)
            byte[] secretKey = generateSecretKey(context.getMerchantId());
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            
            byte[] hmacBytes = mac.doFinal(seed.getBytes());
            
            // Convert to numeric token
            return convertBytesToNumericToken(hmacBytes);
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate COF token", e);
        }
    }
    
    private byte[] generateSecretKey(String merchantId) {
        // In production, this would fetch the merchant's secret key from secure storage
        // For now, generate a deterministic key based on merchant ID
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec("MASTER_KEY".getBytes(), HMAC_ALGORITHM);
            mac.init(keySpec);
            return mac.doFinal(merchantId.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
    }
    
    private String convertBytesToNumericToken(byte[] bytes) {
        StringBuilder token = new StringBuilder();
        
        // Use ByteBuffer to convert bytes to long values
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        
        // Ensure we generate exactly TOKEN_LENGTH digits
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            if (buffer.hasRemaining()) {
                int digit = Math.abs(buffer.get()) % 10;
                // Ensure first digit is not 0
                if (i == 0 && digit == 0) {
                    digit = 1;
                }
                token.append(digit);
            } else {
                // If we run out of bytes, use modular arithmetic on previous digits
                int previousIndex = Math.max(0, i - 1);
                int digit = (Character.getNumericValue(token.charAt(previousIndex)) + i) % 10;
                token.append(digit);
            }
        }
        
        return token.toString();
    }
    
    @Override
    public boolean validateToken(String token) {
        if (token == null || token.length() != TOKEN_LENGTH) {
            return false;
        }
        
        // COF tokens are numeric and 16 digits long
        return token.matches("\\d{" + TOKEN_LENGTH + "}");
    }
    
    @Override
    public String getAlgorithmType() {
        return "COF";
    }
    
    @Override
    public String getTokenFormat() {
        return "NUMERIC";
    }
}