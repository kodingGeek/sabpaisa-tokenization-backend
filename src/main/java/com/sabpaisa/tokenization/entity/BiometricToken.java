package com.sabpaisa.tokenization.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for biometric tokens
 */
@Entity
@Table(name = "biometric_tokens")
public class BiometricToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String tokenId;
    
    @Column(nullable = false)
    private String enrollmentId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String cardHash;
    
    @Column(nullable = false)
    private String maskedCard;
    
    @Column(columnDefinition = "TEXT")
    private String biometricBinding;
    
    @ElementCollection
    @CollectionTable(name = "required_modalities", joinColumns = @JoinColumn(name = "token_id"))
    @Column(name = "modality")
    private Set<String> requiredModalities = new HashSet<>();
    
    private Double minConfidenceScore;
    
    private Boolean livenessDetectionRequired;
    
    @Column(nullable = false)
    private String status; // ACTIVE, EXPIRED, REVOKED, SUSPENDED
    
    @Column(nullable = false)
    private String securityLevel; // LOW, MEDIUM, HIGH, VERY_HIGH
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    private LocalDateTime lastUsedAt;
    
    private LocalDateTime revokedAt;
    
    private Integer usageCount = 0;
    
    private Integer failedAttempts = 0;
    
    private String lastFailureReason;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = createdAt.plusYears(3);
        }
    }
    
    public void incrementUsageCount() {
        if (usageCount == null) usageCount = 0;
        usageCount++;
        lastUsedAt = LocalDateTime.now();
    }
    
    public void incrementFailedAttempts() {
        if (failedAttempts == null) failedAttempts = 0;
        failedAttempts++;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status) && !isExpired();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }
    
    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
    
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    
    public String getCardHash() { return cardHash; }
    public void setCardHash(String cardHash) { this.cardHash = cardHash; }
    
    public String getMaskedCard() { return maskedCard; }
    public void setMaskedCard(String maskedCard) { this.maskedCard = maskedCard; }
    
    public String getBiometricBinding() { return biometricBinding; }
    public void setBiometricBinding(String biometricBinding) { this.biometricBinding = biometricBinding; }
    
    public Set<String> getRequiredModalities() { return requiredModalities; }
    public void setRequiredModalities(Set<String> requiredModalities) { this.requiredModalities = requiredModalities; }
    
    public Double getMinConfidenceScore() { return minConfidenceScore; }
    public void setMinConfidenceScore(Double minConfidenceScore) { this.minConfidenceScore = minConfidenceScore; }
    
    public Boolean getLivenessDetectionRequired() { return livenessDetectionRequired; }
    public void setLivenessDetectionRequired(Boolean livenessDetectionRequired) { this.livenessDetectionRequired = livenessDetectionRequired; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    
    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }
    
    public String getLastFailureReason() { return lastFailureReason; }
    public void setLastFailureReason(String lastFailureReason) { this.lastFailureReason = lastFailureReason; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}