package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.algorithm.TokenizationAlgorithm;
import com.sabpaisa.tokenization.algorithm.SimpleTokenizationAlgorithm;
import com.sabpaisa.tokenization.algorithm.COFTokenizationAlgorithm;
import com.sabpaisa.tokenization.algorithm.FPETokenizationAlgorithm;
import com.sabpaisa.tokenization.dto.TokenizationContext;
import com.sabpaisa.tokenization.dto.TokenizationRequest;
import com.sabpaisa.tokenization.dto.EnhancedTokenizationRequest;
import com.sabpaisa.tokenization.dto.TokenResponse;
import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.entity.Merchant;
import com.sabpaisa.tokenization.repository.TokenRepository;
import com.sabpaisa.tokenization.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EnhancedTokenizationService {
    
    private final TokenRepository tokenRepository;
    private final MerchantRepository merchantRepository;
    private final SimpleTokenizationAlgorithm simpleAlgorithm;
    private final COFTokenizationAlgorithm cofAlgorithm;
    private final FPETokenizationAlgorithm fpeAlgorithm;
    private final FraudDetectionService fraudDetectionService;
    private final AuditService auditService;
    
    /**
     * Create a new token with comprehensive data capture
     */
    public TokenResponse createToken(EnhancedTokenizationRequest request, HttpServletRequest httpRequest) {
        log.info("Creating token for merchant: {} with algorithm: {}", request.getMerchantId(), request.getAlgorithmType());
        
        // Validate merchant
        Merchant merchant = merchantRepository.findByMerchantId(request.getMerchantId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid merchant ID"));
        
        // Check merchant status
        if (!"ACTIVE".equals(merchant.getStatus())) {
            throw new IllegalStateException("Merchant is not active");
        }
        
        // Validate card number
        if (!isValidCardNumber(request.getCardNumber())) {
            throw new IllegalArgumentException("Invalid card number");
        }
        
        // Extract card details
        CardDetails cardDetails = extractCardDetails(request.getCardNumber());
        
        // Build tokenization context
        TokenizationContext context = TokenizationContext.builder()
            .cardNumber(request.getCardNumber())
            .merchantId(request.getMerchantId())
            .customerId(request.getCustomerId())
            .transactionId(request.getTransactionId())
            .cardBrand(cardDetails.getBrand())
            .cardType(cardDetails.getType())
            .isCof(request.isCof())
            .cofContractId(request.getCofContractId())
            .cofInitialTransactionId(request.getCofInitialTransactionId())
            .ipAddress(getClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .deviceId(request.getDeviceId())
            .additionalData(request.getMetadata())
            .build();
        
        // Run fraud detection
        FraudDetectionService.FraudDetectionResult fraudResult = null;
        if (fraudDetectionService != null) {
            // Create a simple tokenization request for fraud detection
            TokenizationRequest fraudRequest = new TokenizationRequest();
            fraudRequest.setCardNumber(request.getCardNumber());
            fraudRequest.setMerchantId(request.getMerchantId());
            fraudResult = fraudDetectionService.evaluateFraudRisk(fraudRequest, extractHeaders(httpRequest));
            if ("BLOCK".equals(fraudResult.getDecision())) {
                auditService.logSecurityEvent("FRAUD_BLOCK", request.getMerchantId(), 
                    "Tokenization blocked due to fraud risk score: " + fraudResult.getRiskScore());
                throw new SecurityException("Transaction blocked due to fraud risk");
            }
        }
        
        // Select algorithm
        TokenizationAlgorithm algorithm = selectAlgorithm(request.getAlgorithmType());
        
        // Check for existing token
        String cardHash = hashCard(request.getCardNumber(), merchant.getMerchantId());
        Optional<Token> existingToken = tokenRepository.findByCardHashAndMerchantAndAlgorithmType(
            cardHash, merchant, algorithm.getAlgorithmType());
        
        if (existingToken.isPresent() && "ACTIVE".equals(existingToken.get().getStatus())) {
            log.info("Returning existing token for card hash: {}", cardHash);
            Token token = existingToken.get();
            token.incrementUsageCount();
            tokenRepository.save(token);
            auditService.logTokenUsage(token.getMerchant().getMerchantId(), 
                token.getTokenValue(), "EXISTING_TOKEN_RETURNED");
            return convertToResponse(token);
        }
        
        // Generate new token
        String tokenValue = generateUniqueToken(algorithm, context);
        
        // Create token entity
        Token token = new Token();
        token.setTokenValue(tokenValue);
        token.setMaskedPan(maskCardNumber(request.getCardNumber()));
        token.setCardHash(cardHash);
        token.setStatus("ACTIVE");
        token.setMerchant(merchant);
        token.setAlgorithmType(algorithm.getAlgorithmType());
        token.setTokenFormat(algorithm.getTokenFormat());
        
        // Set card details
        token.setCardBrand(cardDetails.getBrand());
        token.setCardType(cardDetails.getType());
        token.setCardBin(cardDetails.getBin());
        token.setCardLast4(cardDetails.getLast4());
        token.setIssuerCountry(cardDetails.getIssuerCountry());
        token.setIssuerBank(cardDetails.getIssuerBank());
        
        // Set customer information
        token.setCustomerId(request.getCustomerId());
        token.setCustomerEmail(request.getCustomerEmail());
        token.setCustomerPhone(request.getCustomerPhone());
        
        // Set transaction information
        token.setTransactionId(request.getTransactionId());
        token.setTransactionAmount(request.getTransactionAmount());
        token.setTransactionCurrency(request.getTransactionCurrency());
        
        // Set security information
        token.setIpAddress(getClientIp(httpRequest));
        token.setUserAgent(httpRequest.getHeader("User-Agent"));
        token.setDeviceId(request.getDeviceId());
        if (fraudResult != null) {
            token.setRiskScore(fraudResult.getRiskScore());
            // Store triggered rules as risk factors
            token.setRiskFactors(fraudResult.getTriggeredRules() != null ? 
                String.join(", ", fraudResult.getTriggeredRules()) : null);
        }
        
        // Set COF information
        token.setIsCof(request.isCof());
        token.setCofContractId(request.getCofContractId());
        token.setCofInitialTransactionId(request.getCofInitialTransactionId());
        
        // Set metadata
        if (request.getMetadata() != null) {
            token.setMetadata(request.getMetadata());
        }
        
        // Save token
        Token savedToken = tokenRepository.save(token);
        
        // Audit log
        auditService.logTokenCreation(savedToken.getMerchant().getMerchantId(), 
            savedToken.getTokenValue(), savedToken.getAlgorithmType());
        
        log.info("Token created successfully: {} for merchant: {}", tokenValue, merchant.getMerchantId());
        
        return convertToResponse(savedToken);
    }
    
    /**
     * Update token status
     */
    public TokenResponse updateTokenStatus(String tokenValue, String newStatus, String merchantId) {
        Token token = tokenRepository.findByTokenValueAndMerchant_MerchantId(tokenValue, merchantId)
            .orElseThrow(() -> new IllegalArgumentException("Token not found"));
        
        String oldStatus = token.getStatus();
        token.setStatus(newStatus);
        
        // Set appropriate timestamp
        switch (newStatus) {
            case "SUSPENDED":
                token.setSuspendedAt(LocalDateTime.now());
                break;
            case "REVOKED":
                token.setRevokedAt(LocalDateTime.now());
                break;
            case "ACTIVE":
                token.setSuspendedAt(null);
                break;
        }
        
        Token updatedToken = tokenRepository.save(token);
        auditService.logSecurityEvent("TOKEN_STATUS_CHANGE", 
            updatedToken.getMerchant().getMerchantId(), 
            String.format("Token %s status changed from %s to %s", tokenValue, oldStatus, newStatus));
        
        return convertToResponse(updatedToken);
    }
    
    /**
     * Delete token (soft delete by revoking)
     */
    public void deleteToken(String tokenValue, String merchantId) {
        Token token = tokenRepository.findByTokenValueAndMerchant_MerchantId(tokenValue, merchantId)
            .orElseThrow(() -> new IllegalArgumentException("Token not found"));
        
        token.setStatus("REVOKED");
        token.setRevokedAt(LocalDateTime.now());
        tokenRepository.save(token);
        
        auditService.logSecurityEvent("TOKEN_DELETION", 
            token.getMerchant().getMerchantId(), 
            String.format("Token %s revoked", tokenValue));
    }
    
    /**
     * Get token details
     */
    public TokenResponse getToken(String tokenValue, String merchantId) {
        Token token = tokenRepository.findByTokenValueAndMerchant_MerchantId(tokenValue, merchantId)
            .orElseThrow(() -> new IllegalArgumentException("Token not found"));
        
        token.incrementUsageCount();
        tokenRepository.save(token);
        
        return convertToResponse(token);
    }
    
    /**
     * Search tokens with filtering
     */
    public Page<TokenResponse> searchTokens(String merchantId, String status, String algorithmType, 
                                           String cardBrand, LocalDateTime fromDate, LocalDateTime toDate, 
                                           Pageable pageable) {
        Page<Token> tokens;
        
        if (merchantId != null) {
            // Find merchant and get tokens for that merchant only
            Merchant merchant = merchantRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid merchant ID"));
            tokens = tokenRepository.findByMerchant(merchant, pageable);
        } else {
            // Get all tokens
            tokens = tokenRepository.findAll(pageable);
        }
        
        return tokens.map(this::convertToResponse);
    }
    
    private TokenizationAlgorithm selectAlgorithm(String algorithmType) {
        if (algorithmType == null) {
            return simpleAlgorithm;
        }
        
        switch (algorithmType.toUpperCase()) {
            case "COF":
                return cofAlgorithm;
            case "FPE":
                return fpeAlgorithm;
            case "SIMPLE":
            default:
                return simpleAlgorithm;
        }
    }
    
    private String generateUniqueToken(TokenizationAlgorithm algorithm, TokenizationContext context) {
        String token;
        int attempts = 0;
        
        do {
            token = algorithm.tokenize(context);
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Failed to generate unique token after 10 attempts");
            }
        } while (tokenRepository.existsByTokenValue(token));
        
        return token;
    }
    
    private CardDetails extractCardDetails(String cardNumber) {
        CardDetails details = new CardDetails();
        String cleanedNumber = cardNumber.replaceAll("\\s+", "");
        
        // Extract BIN and last 4
        details.setBin(cleanedNumber.substring(0, Math.min(8, cleanedNumber.length())));
        details.setLast4(cleanedNumber.substring(cleanedNumber.length() - 4));
        
        // Identify card brand
        if (cleanedNumber.startsWith("4")) {
            details.setBrand("VISA");
        } else if (cleanedNumber.startsWith("5")) {
            details.setBrand("MASTERCARD");
        } else if (cleanedNumber.startsWith("3")) {
            details.setBrand("AMEX");
        } else if (cleanedNumber.startsWith("6")) {
            details.setBrand("DISCOVER");
        } else {
            details.setBrand("OTHER");
        }
        
        // Default card type (would be enhanced with BIN lookup in production)
        details.setType("CREDIT");
        details.setIssuerCountry("IN");
        details.setIssuerBank("Unknown Bank");
        
        return details;
    }
    
    private String hashCard(String cardNumber, String merchantId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String toHash = cardNumber + ":" + merchantId;
            byte[] hash = digest.digest(toHash.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash card", e);
        }
    }
    
    private String maskCardNumber(String cardNumber) {
        String cleaned = cardNumber.replaceAll("\\s+", "");
        if (cleaned.length() <= 10) {
            return cleaned;
        }
        
        String first6 = cleaned.substring(0, 6);
        String last4 = cleaned.substring(cleaned.length() - 4);
        int maskLength = cleaned.length() - 10;
        
        return first6 + "*".repeat(maskLength) + last4;
    }
    
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        String cleaned = cardNumber.replaceAll("\\s+", "");
        if (!cleaned.matches("\\d+")) {
            return false;
        }
        
        // Luhn algorithm validation
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
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        
        return headers;
    }
    
    private TokenResponse convertToResponse(Token token) {
        TokenResponse response = new TokenResponse();
        response.setTokenValue(token.getTokenValue());
        response.setMaskedPan(token.getMaskedPan());
        response.setStatus(token.getStatus());
        response.setMerchantId(token.getMerchant().getMerchantId());
        response.setMerchantName(token.getMerchant().getBusinessName());
        response.setAlgorithmType(token.getAlgorithmType());
        response.setTokenFormat(token.getTokenFormat());
        response.setCardBrand(token.getCardBrand());
        response.setCardType(token.getCardType());
        response.setCardLast4(token.getCardLast4());
        response.setCreatedAt(token.getCreatedAt());
        response.setExpiresAt(token.getExpiresAt());
        response.setUsageCount(token.getUsageCount());
        response.setMetadata(token.getMetadata());
        
        return response;
    }
    
    @lombok.Data
    private static class CardDetails {
        private String bin;
        private String last4;
        private String brand;
        private String type;
        private String issuerCountry;
        private String issuerBank;
    }
}