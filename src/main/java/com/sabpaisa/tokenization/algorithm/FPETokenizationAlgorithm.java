package com.sabpaisa.tokenization.algorithm;

import com.sabpaisa.tokenization.dto.TokenizationContext;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Format Preserving Encryption (FPE) tokenization algorithm
 * Preserves the format of the original card number while encrypting it
 */
@Component
public class FPETokenizationAlgorithm implements TokenizationAlgorithm {
    
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    
    @Override
    public String tokenize(TokenizationContext context) {
        String cardNumber = context.getCardNumber().replaceAll("\\s+", "");
        
        try {
            // Generate encryption key based on merchant ID
            byte[] key = generateKey(context.getMerchantId());
            
            // Create IV from card number hash (deterministic but unique per card)
            byte[] iv = generateIV(cardNumber);
            
            // Encrypt the card number
            String encrypted = encrypt(cardNumber, key, iv);
            
            // Convert to format-preserving token
            return formatPreservingTransform(encrypted, cardNumber);
            
        } catch (Exception e) {
            throw new RuntimeException("FPE tokenization failed", e);
        }
    }
    
    private byte[] generateKey(String merchantId) throws Exception {
        // In production, fetch merchant-specific key from secure storage
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((merchantId + "FPE_KEY").getBytes(StandardCharsets.UTF_8));
        // Use first 16 bytes for AES-128
        return Arrays.copyOf(hash, 16);
    }
    
    private byte[] generateIV(String cardNumber) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        return digest.digest(cardNumber.getBytes(StandardCharsets.UTF_8));
    }
    
    private String encrypt(String plaintext, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, KEY_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encrypted);
    }
    
    private String formatPreservingTransform(String encrypted, String originalCardNumber) {
        StringBuilder token = new StringBuilder();
        int encryptedIndex = 0;
        
        // Preserve the length and numeric format of the original card number
        for (int i = 0; i < originalCardNumber.length(); i++) {
            if (encryptedIndex >= encrypted.length()) {
                encryptedIndex = 0; // Wrap around if needed
            }
            
            // Convert hex character to digit (0-9)
            char hexChar = encrypted.charAt(encryptedIndex++);
            int digit = Character.digit(hexChar, 16) % 10;
            
            // Ensure first digit is not 0 (invalid for card numbers)
            if (i == 0 && digit == 0) {
                digit = 1;
            }
            
            token.append(digit);
        }
        
        // Ensure the token passes Luhn check (like a real card number)
        return adjustForLuhn(token.toString());
    }
    
    private String adjustForLuhn(String number) {
        // Calculate Luhn checksum
        int sum = 0;
        boolean alternate = false;
        
        for (int i = number.length() - 2; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        // Calculate the check digit
        int checkDigit = (10 - (sum % 10)) % 10;
        
        // Replace last digit with check digit
        return number.substring(0, number.length() - 1) + checkDigit;
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    @Override
    public boolean validateToken(String token) {
        if (token == null || token.length() < 13 || token.length() > 19) {
            return false;
        }
        
        // Check if all characters are digits
        if (!token.matches("\\d+")) {
            return false;
        }
        
        // Validate using Luhn algorithm
        return isValidLuhn(token);
    }
    
    private boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
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
    
    @Override
    public String getAlgorithmType() {
        return "FPE";
    }
    
    @Override
    public String getTokenFormat() {
        return "PRESERVE_FORMAT";
    }
}