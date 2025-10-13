package com.sabpaisa.tokenization.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "merchants", indexes = {
    @Index(name = "idx_merchant_id", columnList = "merchantId"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_business_type", columnList = "business_type"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
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
    private String status = "PENDING"; // PENDING, ACTIVE, SUSPENDED, INACTIVE, BLOCKED
    
    // Registration and Verification Details
    @Column(name = "registration_number")
    private String registrationNumber;
    
    @Column(name = "incorporation_date")
    private LocalDate incorporationDate;
    
    @Column(name = "business_category")
    private String businessCategory; // E-COMMERCE, RETAIL, SAAS, FINANCIAL_SERVICES, etc.
    
    @Column(name = "mcc_code")
    private String mccCode; // Merchant Category Code
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @Column(name = "country_code", length = 2)
    private String countryCode = "IN";
    
    @Column(name = "state_code", length = 10)
    private String stateCode;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    // Contact Information
    @Column(name = "primary_contact_name")
    private String primaryContactName;
    
    @Column(name = "primary_contact_email")
    private String primaryContactEmail;
    
    @Column(name = "primary_contact_phone")
    private String primaryContactPhone;
    
    @Column(name = "secondary_contact_name")
    private String secondaryContactName;
    
    @Column(name = "secondary_contact_email")
    private String secondaryContactEmail;
    
    @Column(name = "secondary_contact_phone")
    private String secondaryContactPhone;
    
    // Financial Information
    @Column(name = "bank_account_number")
    private String bankAccountNumber;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "bank_ifsc_code")
    private String bankIfscCode;
    
    @Column(name = "bank_branch")
    private String bankBranch;
    
    @Column(name = "account_holder_name")
    private String accountHolderName;
    
    @Column(name = "settlement_currency", length = 3)
    private String settlementCurrency = "INR";
    
    // Business Metrics
    @Column(name = "annual_revenue")
    private BigDecimal annualRevenue;
    
    @Column(name = "monthly_transaction_volume")
    private BigDecimal monthlyTransactionVolume;
    
    @Column(name = "average_transaction_value")
    private BigDecimal averageTransactionValue;
    
    @Column(name = "expected_monthly_tokens")
    private Integer expectedMonthlyTokens;
    
    // Risk and Compliance
    @Column(name = "risk_rating")
    private String riskRating = "MEDIUM"; // LOW, MEDIUM, HIGH, VERY_HIGH
    
    @Column(name = "risk_score")
    private Integer riskScore;
    
    @Column(name = "kyc_status")
    private String kycStatus = "PENDING"; // PENDING, IN_PROGRESS, VERIFIED, REJECTED
    
    @Column(name = "kyc_verified_at")
    private LocalDateTime kycVerifiedAt;
    
    @Column(name = "kyc_verified_by")
    private String kycVerifiedBy;
    
    @Column(name = "kyc_rejection_reason", columnDefinition = "TEXT")
    private String kycRejectionReason;
    
    @Column(name = "compliance_status")
    private String complianceStatus = "PENDING"; // PENDING, COMPLIANT, NON_COMPLIANT
    
    @Column(name = "last_compliance_check")
    private LocalDateTime lastComplianceCheck;
    
    @Column(name = "pci_dss_compliant")
    private Boolean pciDssCompliant = false;
    
    @Column(name = "pci_dss_level")
    private String pciDssLevel; // LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4
    
    @Column(name = "pci_dss_certificate_expiry")
    private LocalDate pciDssCertificateExpiry;
    
    // API and Security Settings
    @Column(name = "ip_whitelist", columnDefinition = "TEXT")
    private String ipWhitelist; // JSON array of whitelisted IPs
    
    @Column(name = "allowed_origins", columnDefinition = "TEXT")
    private String allowedOrigins; // JSON array of allowed CORS origins
    
    @Column(name = "api_rate_limit")
    private Integer apiRateLimit = 1000; // requests per hour
    
    @Column(name = "webhook_secret")
    private String webhookSecret;
    
    @Column(name = "webhook_retry_enabled")
    private Boolean webhookRetryEnabled = true;
    
    @Column(name = "webhook_max_retries")
    private Integer webhookMaxRetries = 3;
    
    @Column(name = "api_version")
    private String apiVersion = "v2";
    
    @Column(name = "encryption_key_id")
    private String encryptionKeyId;
    
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;
    
    // Notification Settings
    @Column(name = "notification_email")
    private String notificationEmail;
    
    @Column(name = "sms_notifications_enabled")
    private Boolean smsNotificationsEnabled = true;
    
    @Column(name = "email_notifications_enabled")
    private Boolean emailNotificationsEnabled = true;
    
    @Column(name = "webhook_notifications_enabled")
    private Boolean webhookNotificationsEnabled = true;
    
    // Billing and Subscription
    @Column(name = "billing_cycle")
    private String billingCycle = "MONTHLY"; // MONTHLY, QUARTERLY, YEARLY
    
    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;
    
    @Column(name = "credit_limit")
    private BigDecimal creditLimit;
    
    @Column(name = "current_balance")
    private BigDecimal currentBalance = BigDecimal.ZERO;
    
    @Column(name = "auto_debit_enabled")
    private Boolean autoDebitEnabled = false;
    
    @Column(name = "payment_method")
    private String paymentMethod = "BANK_TRANSFER"; // BANK_TRANSFER, CREDIT_CARD, UPI
    
    // Usage Statistics
    @Column(name = "total_tokens_created")
    private Long totalTokensCreated = 0L;
    
    @Column(name = "active_tokens_count")
    private Long activeTokensCount = 0L;
    
    @Column(name = "tokens_created_today")
    private Long tokensCreatedToday = 0L;
    
    @Column(name = "tokens_created_this_month")
    private Long tokensCreatedThisMonth = 0L;
    
    @Column(name = "last_token_created_at")
    private LocalDateTime lastTokenCreatedAt;
    
    @Column(name = "last_api_call_at")
    private LocalDateTime lastApiCallAt;
    
    // Feature Flags
    @Column(name = "features_enabled", columnDefinition = "TEXT")
    private String featuresEnabled = "[]"; // JSON array of enabled features
    
    @Column(name = "biometric_tokenization_enabled")
    private Boolean biometricTokenizationEnabled = false;
    
    @Column(name = "quantum_encryption_enabled")
    private Boolean quantumEncryptionEnabled = false;
    
    @Column(name = "platform_tokenization_enabled")
    private Boolean platformTokenizationEnabled = false;
    
    @Column(name = "bulk_operations_enabled")
    private Boolean bulkOperationsEnabled = false;
    
    // Status Timestamps
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;
    
    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;
    
    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;
    
    @Column(name = "status_reason", columnDefinition = "TEXT")
    private String statusReason;
    
    // Additional Metadata
    @ElementCollection
    @CollectionTable(name = "merchant_metadata", joinColumns = @JoinColumn(name = "merchant_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    // Audit Fields
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
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
        if (nextBillingDate == null) {
            nextBillingDate = LocalDate.now().plusMonths(1);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Update status timestamps
        if ("ACTIVE".equals(status) && activatedAt == null) {
            activatedAt = LocalDateTime.now();
        } else if ("SUSPENDED".equals(status) && suspendedAt == null) {
            suspendedAt = LocalDateTime.now();
        } else if ("INACTIVE".equals(status) && deactivatedAt == null) {
            deactivatedAt = LocalDateTime.now();
        } else if ("BLOCKED".equals(status) && blockedAt == null) {
            blockedAt = LocalDateTime.now();
        }
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
    
    // New getters and setters
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    
    public LocalDate getIncorporationDate() {
        return incorporationDate;
    }
    
    public void setIncorporationDate(LocalDate incorporationDate) {
        this.incorporationDate = incorporationDate;
    }
    
    public String getBusinessCategory() {
        return businessCategory;
    }
    
    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }
    
    public String getMccCode() {
        return mccCode;
    }
    
    public void setMccCode(String mccCode) {
        this.mccCode = mccCode;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public String getStateCode() {
        return stateCode;
    }
    
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getPrimaryContactName() {
        return primaryContactName;
    }
    
    public void setPrimaryContactName(String primaryContactName) {
        this.primaryContactName = primaryContactName;
    }
    
    public String getPrimaryContactEmail() {
        return primaryContactEmail;
    }
    
    public void setPrimaryContactEmail(String primaryContactEmail) {
        this.primaryContactEmail = primaryContactEmail;
    }
    
    public String getPrimaryContactPhone() {
        return primaryContactPhone;
    }
    
    public void setPrimaryContactPhone(String primaryContactPhone) {
        this.primaryContactPhone = primaryContactPhone;
    }
    
    public String getSecondaryContactName() {
        return secondaryContactName;
    }
    
    public void setSecondaryContactName(String secondaryContactName) {
        this.secondaryContactName = secondaryContactName;
    }
    
    public String getSecondaryContactEmail() {
        return secondaryContactEmail;
    }
    
    public void setSecondaryContactEmail(String secondaryContactEmail) {
        this.secondaryContactEmail = secondaryContactEmail;
    }
    
    public String getSecondaryContactPhone() {
        return secondaryContactPhone;
    }
    
    public void setSecondaryContactPhone(String secondaryContactPhone) {
        this.secondaryContactPhone = secondaryContactPhone;
    }
    
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }
    
    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getBankIfscCode() {
        return bankIfscCode;
    }
    
    public void setBankIfscCode(String bankIfscCode) {
        this.bankIfscCode = bankIfscCode;
    }
    
    public String getBankBranch() {
        return bankBranch;
    }
    
    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public String getSettlementCurrency() {
        return settlementCurrency;
    }
    
    public void setSettlementCurrency(String settlementCurrency) {
        this.settlementCurrency = settlementCurrency;
    }
    
    public BigDecimal getAnnualRevenue() {
        return annualRevenue;
    }
    
    public void setAnnualRevenue(BigDecimal annualRevenue) {
        this.annualRevenue = annualRevenue;
    }
    
    public BigDecimal getMonthlyTransactionVolume() {
        return monthlyTransactionVolume;
    }
    
    public void setMonthlyTransactionVolume(BigDecimal monthlyTransactionVolume) {
        this.monthlyTransactionVolume = monthlyTransactionVolume;
    }
    
    public BigDecimal getAverageTransactionValue() {
        return averageTransactionValue;
    }
    
    public void setAverageTransactionValue(BigDecimal averageTransactionValue) {
        this.averageTransactionValue = averageTransactionValue;
    }
    
    public Integer getExpectedMonthlyTokens() {
        return expectedMonthlyTokens;
    }
    
    public void setExpectedMonthlyTokens(Integer expectedMonthlyTokens) {
        this.expectedMonthlyTokens = expectedMonthlyTokens;
    }
    
    public String getRiskRating() {
        return riskRating;
    }
    
    public void setRiskRating(String riskRating) {
        this.riskRating = riskRating;
    }
    
    public Integer getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
    
    public String getKycStatus() {
        return kycStatus;
    }
    
    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }
    
    public LocalDateTime getKycVerifiedAt() {
        return kycVerifiedAt;
    }
    
    public void setKycVerifiedAt(LocalDateTime kycVerifiedAt) {
        this.kycVerifiedAt = kycVerifiedAt;
    }
    
    public String getKycVerifiedBy() {
        return kycVerifiedBy;
    }
    
    public void setKycVerifiedBy(String kycVerifiedBy) {
        this.kycVerifiedBy = kycVerifiedBy;
    }
    
    public String getKycRejectionReason() {
        return kycRejectionReason;
    }
    
    public void setKycRejectionReason(String kycRejectionReason) {
        this.kycRejectionReason = kycRejectionReason;
    }
    
    public String getComplianceStatus() {
        return complianceStatus;
    }
    
    public void setComplianceStatus(String complianceStatus) {
        this.complianceStatus = complianceStatus;
    }
    
    public LocalDateTime getLastComplianceCheck() {
        return lastComplianceCheck;
    }
    
    public void setLastComplianceCheck(LocalDateTime lastComplianceCheck) {
        this.lastComplianceCheck = lastComplianceCheck;
    }
    
    public Boolean getPciDssCompliant() {
        return pciDssCompliant;
    }
    
    public void setPciDssCompliant(Boolean pciDssCompliant) {
        this.pciDssCompliant = pciDssCompliant;
    }
    
    public String getPciDssLevel() {
        return pciDssLevel;
    }
    
    public void setPciDssLevel(String pciDssLevel) {
        this.pciDssLevel = pciDssLevel;
    }
    
    public LocalDate getPciDssCertificateExpiry() {
        return pciDssCertificateExpiry;
    }
    
    public void setPciDssCertificateExpiry(LocalDate pciDssCertificateExpiry) {
        this.pciDssCertificateExpiry = pciDssCertificateExpiry;
    }
    
    public String getIpWhitelist() {
        return ipWhitelist;
    }
    
    public void setIpWhitelist(String ipWhitelist) {
        this.ipWhitelist = ipWhitelist;
    }
    
    public String getAllowedOrigins() {
        return allowedOrigins;
    }
    
    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
    
    public Integer getApiRateLimit() {
        return apiRateLimit;
    }
    
    public void setApiRateLimit(Integer apiRateLimit) {
        this.apiRateLimit = apiRateLimit;
    }
    
    public String getWebhookSecret() {
        return webhookSecret;
    }
    
    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }
    
    public Boolean getWebhookRetryEnabled() {
        return webhookRetryEnabled;
    }
    
    public void setWebhookRetryEnabled(Boolean webhookRetryEnabled) {
        this.webhookRetryEnabled = webhookRetryEnabled;
    }
    
    public Integer getWebhookMaxRetries() {
        return webhookMaxRetries;
    }
    
    public void setWebhookMaxRetries(Integer webhookMaxRetries) {
        this.webhookMaxRetries = webhookMaxRetries;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }
    
    public void setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
    }
    
    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }
    
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }
    
    public String getNotificationEmail() {
        return notificationEmail;
    }
    
    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }
    
    public Boolean getSmsNotificationsEnabled() {
        return smsNotificationsEnabled;
    }
    
    public void setSmsNotificationsEnabled(Boolean smsNotificationsEnabled) {
        this.smsNotificationsEnabled = smsNotificationsEnabled;
    }
    
    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }
    
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }
    
    public Boolean getWebhookNotificationsEnabled() {
        return webhookNotificationsEnabled;
    }
    
    public void setWebhookNotificationsEnabled(Boolean webhookNotificationsEnabled) {
        this.webhookNotificationsEnabled = webhookNotificationsEnabled;
    }
    
    public String getBillingCycle() {
        return billingCycle;
    }
    
    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }
    
    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }
    
    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }
    
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }
    
    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public Boolean getAutoDebitEnabled() {
        return autoDebitEnabled;
    }
    
    public void setAutoDebitEnabled(Boolean autoDebitEnabled) {
        this.autoDebitEnabled = autoDebitEnabled;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public Long getTotalTokensCreated() {
        return totalTokensCreated;
    }
    
    public void setTotalTokensCreated(Long totalTokensCreated) {
        this.totalTokensCreated = totalTokensCreated;
    }
    
    public Long getActiveTokensCount() {
        return activeTokensCount;
    }
    
    public void setActiveTokensCount(Long activeTokensCount) {
        this.activeTokensCount = activeTokensCount;
    }
    
    public Long getTokensCreatedToday() {
        return tokensCreatedToday;
    }
    
    public void setTokensCreatedToday(Long tokensCreatedToday) {
        this.tokensCreatedToday = tokensCreatedToday;
    }
    
    public Long getTokensCreatedThisMonth() {
        return tokensCreatedThisMonth;
    }
    
    public void setTokensCreatedThisMonth(Long tokensCreatedThisMonth) {
        this.tokensCreatedThisMonth = tokensCreatedThisMonth;
    }
    
    public LocalDateTime getLastTokenCreatedAt() {
        return lastTokenCreatedAt;
    }
    
    public void setLastTokenCreatedAt(LocalDateTime lastTokenCreatedAt) {
        this.lastTokenCreatedAt = lastTokenCreatedAt;
    }
    
    public LocalDateTime getLastApiCallAt() {
        return lastApiCallAt;
    }
    
    public void setLastApiCallAt(LocalDateTime lastApiCallAt) {
        this.lastApiCallAt = lastApiCallAt;
    }
    
    public String getFeaturesEnabled() {
        return featuresEnabled;
    }
    
    public void setFeaturesEnabled(String featuresEnabled) {
        this.featuresEnabled = featuresEnabled;
    }
    
    public Boolean getBiometricTokenizationEnabled() {
        return biometricTokenizationEnabled;
    }
    
    public void setBiometricTokenizationEnabled(Boolean biometricTokenizationEnabled) {
        this.biometricTokenizationEnabled = biometricTokenizationEnabled;
    }
    
    public Boolean getQuantumEncryptionEnabled() {
        return quantumEncryptionEnabled;
    }
    
    public void setQuantumEncryptionEnabled(Boolean quantumEncryptionEnabled) {
        this.quantumEncryptionEnabled = quantumEncryptionEnabled;
    }
    
    public Boolean getPlatformTokenizationEnabled() {
        return platformTokenizationEnabled;
    }
    
    public void setPlatformTokenizationEnabled(Boolean platformTokenizationEnabled) {
        this.platformTokenizationEnabled = platformTokenizationEnabled;
    }
    
    public Boolean getBulkOperationsEnabled() {
        return bulkOperationsEnabled;
    }
    
    public void setBulkOperationsEnabled(Boolean bulkOperationsEnabled) {
        this.bulkOperationsEnabled = bulkOperationsEnabled;
    }
    
    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }
    
    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }
    
    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }
    
    public void setSuspendedAt(LocalDateTime suspendedAt) {
        this.suspendedAt = suspendedAt;
    }
    
    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }
    
    public void setDeactivatedAt(LocalDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }
    
    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }
    
    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }
    
    public String getStatusReason() {
        return statusReason;
    }
    
    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
    
    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}