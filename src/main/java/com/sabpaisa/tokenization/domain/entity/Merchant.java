package com.sabpaisa.tokenization.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "merchants")
public class Merchant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String businessName;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "business_type")
    private String businessType;
    
    @Column(name = "business_address", length = 500)
    private String businessAddress;
    
    @Column(name = "pan_number")
    private String panNumber;
    
    @Column(name = "gst_number")
    private String gstNumber;
    
    @Column(nullable = false)
    private String status; // ACTIVE, SUSPENDED, INACTIVE
    
    @Column(name = "webhook_url")
    private String webhookUrl;
    
    @Column(nullable = false)
    private String apiKey;
    
    @Column(nullable = false)
    private String apiSecret;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    private Set<Token> tokens = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_plan_id")
    private com.sabpaisa.tokenization.domain.entity.PricingPlan pricingPlan;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Merchant() {}
    
    public Merchant(String merchantId, String businessName, String email) {
        this.merchantId = merchantId;
        this.businessName = businessName;
        this.email = email;
        this.status = "ACTIVE";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getBusinessName() {
        return businessName;
    }
    
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getBusinessType() {
        return businessType;
    }
    
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    public String getBusinessAddress() {
        return businessAddress;
    }
    
    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }
    
    public String getPanNumber() {
        return panNumber;
    }
    
    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }
    
    public String getGstNumber() {
        return gstNumber;
    }
    
    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiSecret() {
        return apiSecret;
    }
    
    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }
    
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Set<Token> getTokens() {
        return tokens;
    }
    
    public void setTokens(Set<Token> tokens) {
        this.tokens = tokens;
    }
    
    public com.sabpaisa.tokenization.domain.entity.PricingPlan getPricingPlan() {
        return pricingPlan;
    }
    
    public void setPricingPlan(com.sabpaisa.tokenization.domain.entity.PricingPlan pricingPlan) {
        this.pricingPlan = pricingPlan;
    }
}