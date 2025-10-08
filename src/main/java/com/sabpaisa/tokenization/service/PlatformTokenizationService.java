package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.domain.entity.*;
import com.sabpaisa.tokenization.entity.Merchant;
import com.sabpaisa.tokenization.presentation.dto.*;
import com.sabpaisa.tokenization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlatformTokenizationService {
    
    private final EnhancedTokenRepository tokenRepository;
    private final MerchantRepository merchantRepository;
    private final PlatformRepository platformRepository;
    private final TokenTypeRepository tokenTypeRepository;
    private final TokenizationService tokenizationService;
    private final AuditService auditService;
    private final TokenMonetizationService monetizationService;
    
    public EnhancedToken createPlatformToken(PlatformTokenizationRequest request, String merchantId) {
        // Validate merchant
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        // Validate platform
        Platform platform = platformRepository.findById(request.getPlatformId())
            .orElseThrow(() -> new RuntimeException("Platform not found"));
        
        // Validate token type
        TokenType tokenType = tokenTypeRepository.findByTypeCode(request.getTokenTypeCode())
            .orElseThrow(() -> new RuntimeException("Token type not found"));
        
        // Generate card hash
        String cardHash = hashCard(request.getCardNumber());
        
        // Check existing tokens for this card and platform
        List<EnhancedToken> existingTokens = tokenRepository
            .findActiveTokensByCardAndPlatform(cardHash, platform.getId());
        
        if (existingTokens.size() >= tokenType.getMaxTokensPerCard()) {
            throw new RuntimeException("Maximum tokens limit reached for this card on this platform");
        }
        
        // Create enhanced token
        EnhancedToken token = new EnhancedToken();
        token.setTokenValue(tokenizationService.generateTokenValue());
        token.setMaskedPan(maskCardNumber(request.getCardNumber()));
        token.setCardHash(cardHash);
        token.setCardBin(request.getCardNumber().substring(0, 6));
        token.setCardLast4(request.getCardNumber().substring(request.getCardNumber().length() - 4));
        token.setCardType(detectCardType(request.getCardNumber()));
        token.setCardBrand(detectCardBrand(request.getCardNumber()));
        
        // Set relationships
        token.setMerchant(merchant);
        token.setPlatform(platform);
        token.setTokenType(tokenType);
        
        // Set expiry
        if (request.getCustomExpiryMonths() != null) {
            token.setExpiryDate(LocalDateTime.now().plusMonths(request.getCustomExpiryMonths()));
        } else {
            token.setExpiryDate(LocalDateTime.now().plusDays(tokenType.getDefaultExpiryDays()));
        }
        
        // Set customer info
        token.setCustomerEmail(request.getCustomerEmail());
        token.setCustomerPhone(request.getCustomerPhone());
        token.setCustomerId(request.getCustomerId());
        
        // Set notification preferences
        token.setNotificationEnabled(request.isEnableNotifications());
        token.setDaysBeforeExpiryNotification(request.getDaysBeforeExpiryNotification());
        
        // Save token
        token = tokenRepository.save(token);
        
        // Log and track
        auditService.logTokenCreation(merchantId, token.getTokenValue(), platform.getPlatformName());
        monetizationService.recordTokenUsage(token, "TOKEN_CREATION");
        
        log.info("Created platform token for merchant: {}, platform: {}", merchantId, platform.getPlatformCode());
        
        return token;
    }
    
    public List<PlatformInfo> getMerchantPlatforms(String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        return platformRepository.findByMerchantAndIsActiveTrue(merchant).stream()
            .map(platform -> {
                PlatformInfo info = new PlatformInfo();
                info.setId(platform.getId());
                info.setPlatformCode(platform.getPlatformCode());
                info.setPlatformName(platform.getPlatformName());
                info.setDescription(platform.getDescription());
                info.setIconUrl(platform.getIconUrl());
                return info;
            })
            .collect(Collectors.toList());
    }
    
    public List<TokenTypeInfo> getActiveTokenTypes() {
        return tokenTypeRepository.findByIsActiveTrue().stream()
            .map(type -> {
                TokenTypeInfo info = new TokenTypeInfo();
                info.setTypeCode(type.getTypeCode());
                info.setTypeName(type.getTypeName());
                info.setDescription(type.getDescription());
                info.setDefaultExpiryDays(type.getDefaultExpiryDays());
                info.setMaxTokensPerCard(type.getMaxTokensPerCard());
                return info;
            })
            .collect(Collectors.toList());
    }
    
    public List<CardTokenInfo> getTokensForCard(String cardHash, String merchantId) {
        return tokenRepository.findActiveTokensByCardAndMerchant(cardHash, merchantId).stream()
            .map(token -> {
                CardTokenInfo info = new CardTokenInfo();
                info.setTokenId(token.getId());
                info.setTokenValue(maskToken(token.getTokenValue()));
                info.setPlatformName(token.getPlatform() != null ? token.getPlatform().getPlatformName() : "All Platforms");
                info.setTokenType(token.getTokenType().getTypeName());
                info.setExpiryDate(token.getExpiryDate());
                info.setIsActive(token.getIsActive());
                return info;
            })
            .collect(Collectors.toList());
    }
    
    public Platform createPlatform(CreatePlatformRequest request, String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        Platform platform = new Platform();
        platform.setPlatformCode(request.getPlatformCode());
        platform.setPlatformName(request.getPlatformName());
        platform.setDescription(request.getDescription());
        platform.setIconUrl(request.getIconUrl());
        platform.setWebhookUrl(request.getWebhookUrl());
        platform.setAllowedDomains(request.getAllowedDomains());
        platform.setMerchant(merchant);
        
        return platformRepository.save(platform);
    }
    
    private String hashCard(String cardNumber) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash card", e);
        }
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 10) return cardNumber;
        
        String firstSix = cardNumber.substring(0, 6);
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        String masked = "*".repeat(cardNumber.length() - 10);
        
        return firstSix + masked + lastFour;
    }
    
    private String maskToken(String token) {
        if (token == null || token.length() < 8) return token;
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
    
    private String detectCardType(String cardNumber) {
        // Simple card type detection based on BIN
        if (cardNumber.startsWith("4")) return "CREDIT";
        if (cardNumber.startsWith("5")) return "DEBIT";
        if (cardNumber.startsWith("3")) return "CREDIT";
        return "UNKNOWN";
    }
    
    private String detectCardBrand(String cardNumber) {
        // Simple card brand detection
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("37")) return "AMEX";
        if (cardNumber.startsWith("6")) return "DISCOVER";
        return "OTHER";
    }
}