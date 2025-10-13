package com.sabpaisa.tokenization.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "tokens", indexes = {
    @Index(name = "idx_token_value", columnList = "tokenValue"),
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_card_hash", columnList = "cardHash"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class Token {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String tokenValue;
    
    @Column(nullable = false, length = 20)
    private String maskedPan; // Masked card number (e.g., 1234****5678)
    
    @Column(nullable = false, length = 100)
    private String cardHash; // Hashed original card data
    
    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, SUSPENDED, REVOKED, EXPIRED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;
    
    // Tokenization Algorithm Details
    @Column(name = "tokenization_mode", length = 20)
    private String algorithmType = "SIMPLE"; // SIMPLE, COF, FPE
    
    @Column(name = "token_format", length = 50)
    private String tokenFormat; // e.g., "NUMERIC", "ALPHANUMERIC", "PRESERVE_FORMAT"
    
    // Card Details
    @Column(name = "card_brand", length = 20)
    private String cardBrand; // VISA, MASTERCARD, AMEX, RUPAY, etc.
    
    @Column(name = "card_type", length = 20)
    private String cardType; // CREDIT, DEBIT, PREPAID
    
    @Column(name = "card_bin", length = 8)
    private String cardBin; // First 6-8 digits of card
    
    @Column(name = "card_last4", length = 4)
    private String cardLast4; // Last 4 digits of card
    
    @Column(name = "issuer_country", length = 2)
    private String issuerCountry; // ISO country code
    
    @Column(name = "issuer_bank", length = 100)
    private String issuerBank;
    
    // Customer Information
    @Column(name = "customer_id", length = 50)
    private String customerId; // Merchant's customer ID
    
    @Column(name = "customer_email", length = 100)
    private String customerEmail;
    
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    // Transaction Information
    @Column(name = "transaction_id", length = 50)
    private String transactionId; // Original transaction ID
    
    @Column(name = "transaction_amount")
    private BigDecimal transactionAmount;
    
    @Column(name = "transaction_currency", length = 3)
    private String transactionCurrency; // ISO currency code
    
    // Timestamps
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    // Usage Statistics
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;
    
    @Column(name = "max_usage_limit")
    private Integer maxUsageLimit; // null means unlimited
    
    // Security Features
    // Note: tokenization_mode column is mapped to algorithmType field above
    
    @Column(name = "encryption_key_id", length = 50)
    private String encryptionKeyId; // Reference to encryption key used
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IP address from which token was created
    
    @Column(name = "user_agent", length = 500)
    private String userAgent; // Browser/app user agent
    
    @Column(name = "device_id", length = 100)
    private String deviceId; // Device fingerprint
    
    @Column(name = "risk_score")
    private Integer riskScore; // Fraud detection risk score (0-100)
    
    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors; // JSON string of risk factors
    
    // COF (Card on File) Specific Fields
    @Column(name = "is_cof")
    private Boolean isCof = false;
    
    @Column(name = "cof_contract_id", length = 50)
    private String cofContractId;
    
    @Column(name = "cof_initial_transaction_id", length = 50)
    private String cofInitialTransactionId;
    
    // Additional Metadata
    @ElementCollection
    @CollectionTable(name = "token_metadata", joinColumns = @JoinColumn(name = "token_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusYears(3); // Default 3 year expiry
        }
        if (cardLast4 == null && maskedPan != null && maskedPan.length() >= 4) {
            cardLast4 = maskedPan.substring(maskedPan.length() - 4);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Token() {}
    
    public Token(String tokenValue, String maskedPan, String cardHash, Merchant merchant) {
        this.tokenValue = tokenValue;
        this.maskedPan = maskedPan;
        this.cardHash = cardHash;
        this.merchant = merchant;
        this.status = "ACTIVE";
        this.usageCount = 0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTokenValue() {
        return tokenValue;
    }
    
    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }
    
    public String getMaskedPan() {
        return maskedPan;
    }
    
    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }
    
    public String getCardHash() {
        return cardHash;
    }
    
    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Merchant getMerchant() {
        return merchant;
    }
    
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public Integer getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
    
    public void incrementUsageCount() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    // tokenizationMode getter/setter removed - using algorithmType instead
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    // New getters and setters
    public String getAlgorithmType() {
        return algorithmType;
    }
    
    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }
    
    public String getTokenFormat() {
        return tokenFormat;
    }
    
    public void setTokenFormat(String tokenFormat) {
        this.tokenFormat = tokenFormat;
    }
    
    public String getCardBrand() {
        return cardBrand;
    }
    
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public String getCardBin() {
        return cardBin;
    }
    
    public void setCardBin(String cardBin) {
        this.cardBin = cardBin;
    }
    
    public String getCardLast4() {
        return cardLast4;
    }
    
    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }
    
    public String getIssuerCountry() {
        return issuerCountry;
    }
    
    public void setIssuerCountry(String issuerCountry) {
        this.issuerCountry = issuerCountry;
    }
    
    public String getIssuerBank() {
        return issuerBank;
    }
    
    public void setIssuerBank(String issuerBank) {
        this.issuerBank = issuerBank;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }
    
    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
    
    public String getTransactionCurrency() {
        return transactionCurrency;
    }
    
    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }
    
    public void setSuspendedAt(LocalDateTime suspendedAt) {
        this.suspendedAt = suspendedAt;
    }
    
    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
    
    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
    
    public Integer getFailedAttempts() {
        return failedAttempts;
    }
    
    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    
    public Integer getMaxUsageLimit() {
        return maxUsageLimit;
    }
    
    public void setMaxUsageLimit(Integer maxUsageLimit) {
        this.maxUsageLimit = maxUsageLimit;
    }
    
    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }
    
    public void setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public Integer getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
    
    public String getRiskFactors() {
        return riskFactors;
    }
    
    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }
    
    public Boolean getIsCof() {
        return isCof;
    }
    
    public void setIsCof(Boolean isCof) {
        this.isCof = isCof;
    }
    
    public String getCofContractId() {
        return cofContractId;
    }
    
    public void setCofContractId(String cofContractId) {
        this.cofContractId = cofContractId;
    }
    
    public String getCofInitialTransactionId() {
        return cofInitialTransactionId;
    }
    
    public void setCofInitialTransactionId(String cofInitialTransactionId) {
        this.cofInitialTransactionId = cofInitialTransactionId;
    }
}