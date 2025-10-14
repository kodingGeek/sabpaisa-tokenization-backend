package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.dto.*;
import com.sabpaisa.tokenization.domain.entity.Merchant;
import com.sabpaisa.tokenization.repository.MerchantRepository;
import com.sabpaisa.tokenization.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MerchantService {
    
    private final MerchantRepository merchantRepository;
    private final TokenRepository tokenRepository;
    private final SecureRandom secureRandom;
    
    @Autowired
    public MerchantService(MerchantRepository merchantRepository, TokenRepository tokenRepository) {
        this.merchantRepository = merchantRepository;
        this.tokenRepository = tokenRepository;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Create a new merchant
     */
    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        // Check if merchant with same email exists
        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Merchant with email " + request.getEmail() + " already exists");
        }
        
        // Generate unique merchant ID
        String merchantId = generateUniqueMerchantId();
        
        // Create merchant entity
        Merchant merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        merchant.setBusinessName(request.getBusinessName());
        merchant.setEmail(request.getEmail());
        merchant.setPhoneNumber(request.getPhoneNumber());
        merchant.setBusinessType(request.getBusinessType());
        merchant.setBusinessAddress(request.getBusinessAddress());
        merchant.setPanNumber(request.getPanNumber());
        merchant.setGstNumber(request.getGstNumber());
        merchant.setWebhookUrl(request.getWebhookUrl());
        merchant.setStatus("ACTIVE");
        
        // Generate API credentials
        String apiKey = generateApiKey();
        String apiSecret = generateApiSecret();
        merchant.setApiKey(apiKey);
        merchant.setApiSecret(apiSecret);
        
        // Save merchant
        merchant = merchantRepository.save(merchant);
        
        // Build response
        return buildMerchantResponse(merchant, true);
    }
    
    /**
     * Get merchant by ID
     */
    public MerchantResponse getMerchantById(String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantId));
        
        return buildMerchantResponse(merchant, false);
    }
    
    /**
     * Update merchant
     */
    public MerchantResponse updateMerchant(String merchantId, UpdateMerchantRequest request) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantId));
        
        // Update fields if provided
        if (request.getBusinessName() != null) {
            merchant.setBusinessName(request.getBusinessName());
        }
        if (request.getEmail() != null) {
            // Check if email is being changed and if new email already exists
            if (!merchant.getEmail().equals(request.getEmail()) && 
                merchantRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use: " + request.getEmail());
            }
            merchant.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            merchant.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBusinessType() != null) {
            merchant.setBusinessType(request.getBusinessType());
        }
        if (request.getBusinessAddress() != null) {
            merchant.setBusinessAddress(request.getBusinessAddress());
        }
        if (request.getWebhookUrl() != null) {
            merchant.setWebhookUrl(request.getWebhookUrl());
        }
        if (request.getStatus() != null) {
            merchant.setStatus(request.getStatus());
        }
        
        merchant = merchantRepository.save(merchant);
        
        return buildMerchantResponse(merchant, false);
    }
    
    /**
     * Get all merchants with pagination
     */
    public MerchantListResponse getAllMerchants(Pageable pageable, String status) {
        Page<Merchant> merchantPage;
        
        if (status != null && !status.isEmpty()) {
            merchantPage = merchantRepository.findByStatus(status, pageable);
        } else {
            merchantPage = merchantRepository.findAll(pageable);
        }
        
        List<MerchantListResponse.MerchantSummary> summaries = merchantPage.getContent().stream()
            .map(this::buildMerchantSummary)
            .collect(Collectors.toList());
        
        return new MerchantListResponse(
            summaries,
            (int) merchantPage.getTotalElements(),
            merchantPage.getTotalPages(),
            merchantPage.getNumber(),
            merchantPage.getSize()
        );
    }
    
    /**
     * Delete merchant (soft delete by setting status to INACTIVE)
     */
    public void deleteMerchant(String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantId));
        
        merchant.setStatus("INACTIVE");
        merchantRepository.save(merchant);
    }
    
    /**
     * Regenerate API credentials for a merchant
     */
    public MerchantResponse regenerateApiCredentials(String merchantId) {
        Merchant merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found: " + merchantId));
        
        // Generate new API credentials
        String apiKey = generateApiKey();
        String apiSecret = generateApiSecret();
        merchant.setApiKey(apiKey);
        merchant.setApiSecret(apiSecret);
        
        merchant = merchantRepository.save(merchant);
        
        return buildMerchantResponse(merchant, true);
    }
    
    /**
     * Generate unique merchant ID
     */
    private String generateUniqueMerchantId() {
        String prefix = "MERCH";
        String merchantId;
        do {
            // Generate 6 digit random number
            int randomNum = 100000 + secureRandom.nextInt(900000);
            merchantId = prefix + randomNum;
        } while (merchantRepository.existsByMerchantId(merchantId));
        
        return merchantId;
    }
    
    /**
     * Generate API key
     */
    private String generateApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "sk_live_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Generate API secret
     */
    private String generateApiSecret() {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Build merchant response DTO
     */
    private MerchantResponse buildMerchantResponse(Merchant merchant, boolean includeFullApiKey) {
        // Get merchant statistics
        MerchantResponse.MerchantStats stats = new MerchantResponse.MerchantStats();
        stats.setTotalTokens(tokenRepository.countByMerchant(merchant));
        stats.setActiveTokens(tokenRepository.countByMerchantAndStatus(merchant, "ACTIVE"));
        
        // Count tokens created today
        LocalDate today = LocalDate.now();
        stats.setTokensCreatedToday(tokenRepository.countByMerchantAndCreatedAtBetween(
            merchant, 
            today.atStartOfDay(), 
            today.plusDays(1).atStartOfDay()
        ));
        
        // Build settings (hardcoded for now, can be made configurable)
        MerchantResponse.MerchantSettings settings = new MerchantResponse.MerchantSettings();
        settings.setAllowRefunds(true);
        settings.setAllowPartialRefunds(false);
        settings.setTokenExpiryDays(1095);
        settings.setMaxTokensPerCard(5);
        settings.setNotifyOnTokenCreation(true);
        
        // Build API credentials
        MerchantResponse.ApiCredentials apiCredentials = new MerchantResponse.ApiCredentials(
            includeFullApiKey ? merchant.getApiKey() : null
        );
        
        return new MerchantResponse.Builder()
            .merchantId(merchant.getMerchantId())
            .businessName(merchant.getBusinessName())
            .email(merchant.getEmail())
            .phoneNumber(merchant.getPhoneNumber())
            .businessType(merchant.getBusinessType())
            .businessAddress(merchant.getBusinessAddress())
            .panNumber(merchant.getPanNumber())
            .gstNumber(merchant.getGstNumber())
            .status(merchant.getStatus())
            .webhookUrl(merchant.getWebhookUrl())
            .apiCredentials(apiCredentials)
            .settings(settings)
            .stats(stats)
            .createdAt(merchant.getCreatedAt())
            .updatedAt(merchant.getUpdatedAt())
            .build();
    }
    
    /**
     * Build merchant summary for list response
     */
    private MerchantListResponse.MerchantSummary buildMerchantSummary(Merchant merchant) {
        long activeTokens = tokenRepository.countByMerchantAndStatus(merchant, "ACTIVE");
        
        return new MerchantListResponse.MerchantSummary(
            merchant.getMerchantId(),
            merchant.getBusinessName(),
            merchant.getEmail(),
            merchant.getBusinessType(),
            merchant.getStatus(),
            activeTokens,
            merchant.getCreatedAt().toString()
        );
    }
}