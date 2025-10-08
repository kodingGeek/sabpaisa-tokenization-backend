package com.sabpaisa.tokenization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for biometric enrollment data
 */
@Entity
@Table(name = "biometric_enrollments")
public class BiometricEnrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String enrollmentId;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, SUSPENDED
    
    @ElementCollection
    @CollectionTable(name = "enabled_modalities", joinColumns = @JoinColumn(name = "enrollment_id"))
    @Column(name = "modality")
    private Set<String> enabledModalities = new HashSet<>();
    
    @Column(columnDefinition = "TEXT")
    private String biometricHash;
    
    @Column(columnDefinition = "TEXT")
    private String antiSpoofingSecret;
    
    private String securityLevel;
    
    private Double overallQualityScore;
    
    @Column(columnDefinition = "TEXT")
    private String facialTemplateData;
    
    @Column(columnDefinition = "TEXT")
    private String fingerprintTemplateData;
    
    @Column(columnDefinition = "TEXT")
    private String voiceTemplateData;
    
    @Column(columnDefinition = "TEXT")
    private String behavioralTemplateData;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastAuthenticationAt;
    
    private LocalDateTime lastKeyRotation;
    
    private Integer authenticationCount = 0;
    
    private Integer failedAuthenticationCount = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void incrementAuthenticationCount() {
        if (authenticationCount == null) authenticationCount = 0;
        authenticationCount++;
    }
    
    public void incrementFailedAuthenticationCount() {
        if (failedAuthenticationCount == null) failedAuthenticationCount = 0;
        failedAuthenticationCount++;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Set<String> getEnabledModalities() { return enabledModalities; }
    public void setEnabledModalities(Set<String> enabledModalities) { this.enabledModalities = enabledModalities; }
    
    public String getBiometricHash() { return biometricHash; }
    public void setBiometricHash(String biometricHash) { this.biometricHash = biometricHash; }
    
    public String getAntiSpoofingSecret() { return antiSpoofingSecret; }
    public void setAntiSpoofingSecret(String antiSpoofingSecret) { this.antiSpoofingSecret = antiSpoofingSecret; }
    
    public String getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
    
    public Double getOverallQualityScore() { return overallQualityScore; }
    public void setOverallQualityScore(Double overallQualityScore) { this.overallQualityScore = overallQualityScore; }
    
    public String getFacialTemplateData() { return facialTemplateData; }
    public void setFacialTemplateData(String facialTemplateData) { this.facialTemplateData = facialTemplateData; }
    
    public String getFingerprintTemplateData() { return fingerprintTemplateData; }
    public void setFingerprintTemplateData(String fingerprintTemplateData) { this.fingerprintTemplateData = fingerprintTemplateData; }
    
    public String getVoiceTemplateData() { return voiceTemplateData; }
    public void setVoiceTemplateData(String voiceTemplateData) { this.voiceTemplateData = voiceTemplateData; }
    
    public String getBehavioralTemplateData() { return behavioralTemplateData; }
    public void setBehavioralTemplateData(String behavioralTemplateData) { this.behavioralTemplateData = behavioralTemplateData; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastAuthenticationAt() { return lastAuthenticationAt; }
    public void setLastAuthenticationAt(LocalDateTime lastAuthenticationAt) { this.lastAuthenticationAt = lastAuthenticationAt; }
    
    public LocalDateTime getLastKeyRotation() { return lastKeyRotation; }
    public void setLastKeyRotation(LocalDateTime lastKeyRotation) { this.lastKeyRotation = lastKeyRotation; }
    
    public Integer getAuthenticationCount() { return authenticationCount; }
    public void setAuthenticationCount(Integer authenticationCount) { this.authenticationCount = authenticationCount; }
    
    public Integer getFailedAuthenticationCount() { return failedAuthenticationCount; }
    public void setFailedAuthenticationCount(Integer failedAuthenticationCount) { this.failedAuthenticationCount = failedAuthenticationCount; }
}