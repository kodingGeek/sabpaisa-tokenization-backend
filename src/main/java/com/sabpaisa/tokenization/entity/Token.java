package com.sabpaisa.tokenization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "tokens")
public class Token {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String tokenValue;
    
    @Column(nullable = false)
    private String maskedPan; // Masked card number (e.g., 1234****5678)
    
    @Column(nullable = false)
    private String cardHash; // Hashed original card data
    
    @Column(nullable = false)
    private String status; // ACTIVE, SUSPENDED, REVOKED, EXPIRED
    
    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "tokenization_mode")
    private String tokenizationMode = "STANDARD"; // STANDARD, BIOMETRIC, QUANTUM, CLOUD_REPLICATED, HYBRID
    
    @ElementCollection
    @CollectionTable(name = "token_metadata", joinColumns = @JoinColumn(name = "token_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusYears(3); // Default 3 year expiry
        }
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
    
    public String getTokenizationMode() {
        return tokenizationMode;
    }
    
    public void setTokenizationMode(String tokenizationMode) {
        this.tokenizationMode = tokenizationMode;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}