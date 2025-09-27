package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.entity.Merchant;
import com.sabpaisa.tokenization.repository.TokenRepository;
import com.sabpaisa.tokenization.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class TokenizationService {
    
    private final TokenRepository tokenRepository;
    private final MerchantRepository merchantRepository;
    private final SecureRandom secureRandom;
    
    @Autowired
    public TokenizationService(TokenRepository tokenRepository, 
                              MerchantRepository merchantRepository) {
        this.tokenRepository = tokenRepository;
        this.merchantRepository = merchantRepository;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Tokenize a card number for a merchant
     */
    public Token tokenizeCard(String cardNumber, String merchantId) {
        // Validate card number
        if (!isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }
        
        // Find merchant
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        // Hash the card number
        String cardHash = hashCard(cardNumber, merchant.getMerchantId());
        
        // Check if token already exists for this card and merchant
        Optional<Token> existingToken = tokenRepository.findByCardHashAndMerchant(cardHash, merchant);
        if (existingToken.isPresent() && "ACTIVE".equals(existingToken.get().getStatus())) {
            return existingToken.get();
        }
        
        // Generate new token
        String tokenValue = generateUniqueToken();
        String maskedPan = maskCardNumber(cardNumber);
        
        // Create and save token
        Token token = new Token(tokenValue, maskedPan, cardHash, merchant);
        return tokenRepository.save(token);
    }
    
    /**
     * Detokenize - retrieve masked card info
     */
    public Token detokenize(String tokenValue, String merchantId) {
        // Find merchant
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        // Find token
        Token token = tokenRepository.findByTokenValue(tokenValue)
            .orElseThrow(() -> new RuntimeException("Token not found"));
        
        // Verify token belongs to merchant
        if (!token.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("Token not found for this merchant");
        }
        
        // Check token status
        if (!"ACTIVE".equals(token.getStatus())) {
            throw new RuntimeException("Token is not active");
        }
        
        // Update usage
        token.incrementUsageCount();
        tokenRepository.save(token);
        
        return token;
    }
    
    /**
     * Validate card number using Luhn algorithm
     */
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        // Remove spaces and validate digits only
        String cleaned = cardNumber.replaceAll("\\s+", "");
        if (!cleaned.matches("\\d+")) {
            return false;
        }
        
        // Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cleaned.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleaned.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
    
    /**
     * Hash card number with merchant salt
     */
    private String hashCard(String cardNumber, String merchantId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String toHash = cardNumber + ":" + merchantId;
            byte[] hash = digest.digest(toHash.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash card", e);
        }
    }
    
    /**
     * Generate unique token value
     */
    private String generateUniqueToken() {
        String token;
        do {
            // Generate 16 digit numeric token
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(secureRandom.nextInt(10));
            }
            token = sb.toString();
        } while (tokenRepository.existsByTokenValue(token));
        
        return token;
    }
    
    /**
     * Mask card number showing first 6 and last 4 digits
     */
    private String maskCardNumber(String cardNumber) {
        String cleaned = cardNumber.replaceAll("\\s+", "");
        if (cleaned.length() <= 10) {
            return cleaned; // Don't mask if too short
        }
        
        String first6 = cleaned.substring(0, 6);
        String last4 = cleaned.substring(cleaned.length() - 4);
        int maskLength = cleaned.length() - 10;
        
        return first6 + "*".repeat(maskLength) + last4;
    }
}