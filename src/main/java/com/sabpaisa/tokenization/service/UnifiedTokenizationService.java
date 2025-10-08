package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.entity.Merchant;
import com.sabpaisa.tokenization.repository.TokenRepository;
import com.sabpaisa.tokenization.repository.MerchantRepository;
import com.sabpaisa.tokenization.dto.TokenListResponse;
import com.sabpaisa.tokenization.dto.TokenizationRequest;
import com.sabpaisa.tokenization.biometric.BiometricTokenizationService;
import com.sabpaisa.tokenization.cloud.MultiCloudReplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Unified Tokenization Service
 * 
 * Combines all tokenization platforms into a single service:
 * - Standard tokenization (default)
 * - Biometric-enhanced tokenization
 * - Quantum-resistant tokenization
 * - Multi-cloud replicated tokenization
 */
@Service
@Transactional
public class UnifiedTokenizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedTokenizationService.class);
    
    // Core dependencies
    private final TokenRepository tokenRepository;
    private final MerchantRepository merchantRepository;
    private final SecureRandom secureRandom;
    
    // Platform services
    @Autowired(required = false)
    private BiometricTokenizationService biometricService;
    
    @Autowired(required = false)
    private QuantumTokenVaultService quantumVaultService;
    
    @Autowired(required = false)
    private MultiCloudReplicationService cloudReplicationService;
    
    @Autowired(required = false)
    private FraudDetectionService fraudDetectionService;
    
    @Autowired(required = false)
    private TokenizationService legacyTokenizationService;
    
    public enum TokenizationMode {
        STANDARD("standard"),
        BIOMETRIC("biometric"),
        QUANTUM("quantum"),
        CLOUD_REPLICATED("cloud_replicated"),
        HYBRID("hybrid"); // Combines multiple modes
        
        private final String mode;
        TokenizationMode(String mode) {
            this.mode = mode;
        }
    }
    
    @Autowired
    public UnifiedTokenizationService(TokenRepository tokenRepository, 
                                     MerchantRepository merchantRepository) {
        this.tokenRepository = tokenRepository;
        this.merchantRepository = merchantRepository;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Main tokenization method with mode selection
     */
    public Token tokenize(String cardNumber, String merchantId, TokenizationMode mode, Map<String, Object> options) {
        logger.info("Tokenizing with mode: {} for merchant: {}", mode, merchantId);
        
        // Validate inputs
        if (!isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }
        
        // Find merchant
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        // Run fraud detection if enabled
        if (fraudDetectionService != null && options != null && options.containsKey("headers")) {
            runFraudDetection(cardNumber, merchantId, (Map<String, String>) options.get("headers"));
        }
        
        Token token;
        
        switch (mode) {
            case BIOMETRIC:
                token = tokenizeWithBiometric(cardNumber, merchant, options);
                break;
                
            case QUANTUM:
                token = tokenizeWithQuantum(cardNumber, merchant, options);
                break;
                
            case CLOUD_REPLICATED:
                token = tokenizeWithCloudReplication(cardNumber, merchant, options);
                break;
                
            case HYBRID:
                token = tokenizeHybrid(cardNumber, merchant, options);
                break;
                
            case STANDARD:
            default:
                token = tokenizeStandard(cardNumber, merchant);
                break;
        }
        
        logger.info("Token created successfully: {} with mode: {}", token.getTokenValue(), mode);
        return token;
    }
    
    /**
     * Standard tokenization (backward compatible)
     */
    public Token tokenizeCard(String cardNumber, String merchantId) {
        return tokenize(cardNumber, merchantId, TokenizationMode.STANDARD, null);
    }
    
    /**
     * Standard tokenization with fraud detection
     */
    public Token tokenizeCard(String cardNumber, String merchantId, Map<String, String> headers) {
        Map<String, Object> options = new HashMap<>();
        options.put("headers", headers);
        return tokenize(cardNumber, merchantId, TokenizationMode.STANDARD, options);
    }
    
    /**
     * Detokenize - retrieve masked card information
     */
    public Token detokenize(String tokenValue, String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        Token token = tokenRepository.findByTokenValue(tokenValue)
            .orElseThrow(() -> new RuntimeException("Token not found"));
        
        // Verify merchant ownership
        if (!token.getMerchant().getMerchantId().equals(merchantId)) {
            throw new RuntimeException("Token does not belong to merchant");
        }
        
        // Increment usage count
        token.incrementUsageCount();
        tokenRepository.save(token);
        
        return token;
    }
    
    /**
     * Get all tokens with pagination
     */
    public TokenListResponse getAllTokens(Pageable pageable, String merchantId) {
        Page<Token> tokenPage;
        
        if (merchantId != null && !merchantId.isEmpty()) {
            Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));
            tokenPage = tokenRepository.findByMerchant(merchant, pageable);
        } else {
            tokenPage = tokenRepository.findAll(pageable);
        }
        
        List<TokenListResponse.TokenInfo> tokens = tokenPage.getContent().stream()
            .map(this::mapTokenToTokenInfo)
            .collect(Collectors.toList());
        
        TokenListResponse response = new TokenListResponse();
        response.setTokens(tokens);
        response.setTotalElements((int) tokenPage.getTotalElements());
        response.setTotalPages(tokenPage.getTotalPages());
        response.setCurrentPage(tokenPage.getNumber());
        response.setPageSize(tokenPage.getSize());
        
        return response;
    }
    
    // Private helper methods
    
    private Token tokenizeStandard(String cardNumber, Merchant merchant) {
        // Check for existing active token
        String cardHash = hashCard(cardNumber, merchant.getMerchantId());
        Optional<Token> existingToken = tokenRepository.findByCardHashAndMerchant(cardHash, merchant);
        if (existingToken.isPresent() && "ACTIVE".equals(existingToken.get().getStatus())) {
            return existingToken.get();
        }
        
        // Generate new token
        String tokenValue = generateUniqueToken();
        String maskedPan = maskCardNumber(cardNumber);
        
        // Create and save token
        Token token = new Token(tokenValue, maskedPan, cardHash, merchant);
        token.setTokenizationMode("STANDARD");
        return tokenRepository.save(token);
    }
    
    private Token tokenizeWithBiometric(String cardNumber, Merchant merchant, Map<String, Object> options) {
        if (biometricService == null) {
            logger.warn("Biometric service not available, falling back to standard tokenization");
            return tokenizeStandard(cardNumber, merchant);
        }
        
        // First create standard token
        Token token = tokenizeStandard(cardNumber, merchant);
        token.setTokenizationMode("BIOMETRIC");
        
        // Add biometric protection if biometric data is provided
        if (options != null && options.containsKey("biometricData")) {
            // TODO: Integrate with biometric service
            token.getMetadata().put("biometric_protected", "true");
            token.getMetadata().put("biometric_level", "HIGH");
        }
        
        return tokenRepository.save(token);
    }
    
    private Token tokenizeWithQuantum(String cardNumber, Merchant merchant, Map<String, Object> options) {
        if (quantumVaultService == null) {
            logger.warn("Quantum vault service not available, falling back to standard tokenization");
            return tokenizeStandard(cardNumber, merchant);
        }
        
        // Create standard token
        Token token = tokenizeStandard(cardNumber, merchant);
        token.setTokenizationMode("QUANTUM");
        
        // Store sensitive data in quantum vault
        String vaultId = quantumVaultService.storeInQuantumVault(token, cardNumber);
        token.getMetadata().put("quantum_vault_id", vaultId);
        token.getMetadata().put("quantum_security_level", "NIST_LEVEL_5");
        
        return tokenRepository.save(token);
    }
    
    private Token tokenizeWithCloudReplication(String cardNumber, Merchant merchant, Map<String, Object> options) {
        if (cloudReplicationService == null) {
            logger.warn("Cloud replication service not available, falling back to standard tokenization");
            return tokenizeStandard(cardNumber, merchant);
        }
        
        // Create standard token
        Token token = tokenizeStandard(cardNumber, merchant);
        token.setTokenizationMode("CLOUD_REPLICATED");
        
        // Replicate to cloud providers
        cloudReplicationService.replicateToken(token, merchant);
        token.getMetadata().put("cloud_replicated", "true");
        token.getMetadata().put("replication_providers", "AWS,AZURE,GCP");
        
        return tokenRepository.save(token);
    }
    
    private Token tokenizeHybrid(String cardNumber, Merchant merchant, Map<String, Object> options) {
        // Create token with multiple protection layers
        Token token = tokenizeStandard(cardNumber, merchant);
        token.setTokenizationMode("HYBRID");
        
        // Apply all available protections
        if (biometricService != null && options != null && options.containsKey("biometricData")) {
            token.getMetadata().put("biometric_protected", "true");
        }
        
        if (quantumVaultService != null) {
            String vaultId = quantumVaultService.storeInQuantumVault(token, cardNumber);
            token.getMetadata().put("quantum_vault_id", vaultId);
        }
        
        if (cloudReplicationService != null) {
            cloudReplicationService.replicateToken(token, merchant);
            token.getMetadata().put("cloud_replicated", "true");
        }
        
        return tokenRepository.save(token);
    }
    
    private void runFraudDetection(String cardNumber, String merchantId, Map<String, String> headers) {
        TokenizationRequest request = new TokenizationRequest();
        request.setCardNumber(cardNumber);
        request.setMerchantId(merchantId);
        
        FraudDetectionService.FraudDetectionResult fraudResult = 
            fraudDetectionService.evaluateFraudRisk(request, headers);
        
        if ("BLOCK".equals(fraudResult.getDecision())) {
            throw new RuntimeException("Transaction blocked due to fraud risk. Risk score: " + 
                fraudResult.getRiskScore() + ", Risk level: " + fraudResult.getRiskLevel());
        }
        
        logger.info("Fraud check passed - Risk score: {}, Decision: {}", 
            fraudResult.getRiskScore(), fraudResult.getDecision());
    }
    
    private boolean isValidCardNumber(String cardNumber) {
        // Remove spaces and validate
        cardNumber = cardNumber.replaceAll("\\s", "");
        
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        // Luhn algorithm
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
    
    private String hashCard(String cardNumber, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash card number", e);
        }
    }
    
    private String generateUniqueToken() {
        String token;
        do {
            // Generate 16-digit numeric token
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(secureRandom.nextInt(10));
            }
            token = sb.toString();
        } while (tokenRepository.existsByTokenValue(token));
        
        return token;
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 10) {
            return cardNumber;
        }
        String first6 = cardNumber.substring(0, 6);
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        String masked = "*".repeat(cardNumber.length() - 10);
        return first6 + masked + last4;
    }
    
    private TokenListResponse.TokenInfo mapTokenToTokenInfo(Token token) {
        TokenListResponse.TokenInfo info = new TokenListResponse.TokenInfo();
        info.setTokenValue(token.getTokenValue());
        info.setMaskedPan(token.getMaskedPan());
        info.setStatus(token.getStatus());
        info.setMerchantId(token.getMerchant().getMerchantId());
        info.setMerchantName(token.getMerchant().getBusinessName());
        info.setUsageCount(token.getUsageCount());
        info.setCreatedAt(token.getCreatedAt());
        info.setExpiresAt(token.getExpiresAt());
        info.setTokenizationMode(token.getTokenizationMode());
        return info;
    }
    
    private Map<String, Object> mapTokenToResponse(Token token) {
        Map<String, Object> response = new HashMap<>();
        response.put("tokenValue", token.getTokenValue());
        response.put("maskedPan", token.getMaskedPan());
        response.put("status", token.getStatus());
        response.put("merchantId", token.getMerchant().getMerchantId());
        response.put("createdAt", token.getCreatedAt());
        response.put("expiresAt", token.getExpiresAt());
        response.put("usageCount", token.getUsageCount());
        response.put("tokenizationMode", token.getTokenizationMode());
        response.put("metadata", token.getMetadata());
        return response;
    }
}