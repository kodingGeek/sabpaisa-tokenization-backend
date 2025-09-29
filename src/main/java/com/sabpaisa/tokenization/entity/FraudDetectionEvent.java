package com.sabpaisa.tokenization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "fraud_detection_events")
public class FraudDetectionEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String eventId;
    
    @Column(nullable = false)
    private String tokenizationRequestId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String eventType; // TOKENIZATION, TRANSACTION, LOGIN, PROFILE_UPDATE
    
    @Column(nullable = false)
    private LocalDateTime eventTimestamp;
    
    // Risk Assessment
    @Column(nullable = false)
    private int riskScore; // 0-100, calculated based on all rules
    
    @Column(nullable = false)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Column(nullable = false)
    private String decision; // ALLOW, BLOCK, CHALLENGE, MANUAL_REVIEW
    
    @ElementCollection
    @CollectionTable(name = "triggered_rules")
    private List<String> triggeredRules;
    
    // Device Information
    @Column(name = "device_fingerprint")
    private String deviceFingerprint;
    
    @Column(name = "device_type")
    private String deviceType;
    
    @Column(name = "browser_info")
    private String browserInfo;
    
    @Column(name = "os_info")
    private String osInfo;
    
    // Location Information
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "geo_country")
    private String geoCountry;
    
    @Column(name = "geo_city")
    private String geoCity;
    
    @Column(name = "geo_latitude")
    private Double geoLatitude;
    
    @Column(name = "geo_longitude")
    private Double geoLongitude;
    
    @Column(name = "is_vpn")
    private boolean isVpn;
    
    @Column(name = "is_proxy")
    private boolean isProxy;
    
    @Column(name = "is_tor")
    private boolean isTor;
    
    // Behavioral Analytics
    @Column(name = "session_duration")
    private Long sessionDuration; // in seconds
    
    @Column(name = "pages_visited")
    private Integer pagesVisited;
    
    @Column(name = "mouse_movements")
    private Integer mouseMovements;
    
    @Column(name = "typing_speed")
    private Double typingSpeed; // words per minute
    
    // ML Model Results
    @Column(name = "ml_anomaly_score")
    private Double mlAnomalyScore;
    
    @Column(name = "ml_fraud_probability")
    private Double mlFraudProbability;
    
    @Column(name = "ml_model_version")
    private String mlModelVersion;
    
    @ElementCollection
    @CollectionTable(name = "ml_feature_scores")
    private Map<String, Double> mlFeatureScores;
    
    // Velocity Checks
    @Column(name = "tokens_last_hour")
    private Integer tokensLastHour;
    
    @Column(name = "tokens_last_day")
    private Integer tokensLastDay;
    
    @Column(name = "unique_cards_last_hour")
    private Integer uniqueCardsLastHour;
    
    @Column(name = "failed_attempts_last_hour")
    private Integer failedAttemptsLastHour;
    
    // Additional Context
    @Column(columnDefinition = "TEXT")
    private String additionalContext; // JSON with any extra information
    
    @Column(name = "manual_review_notes", columnDefinition = "TEXT")
    private String manualReviewNotes;
    
    @Column(name = "reviewed_by")
    private String reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "is_false_positive")
    private Boolean isFalsePositive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        eventTimestamp = LocalDateTime.now();
        if (eventId == null) {
            eventId = "FDE-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        }
    }
    
    // Calculate risk level based on score
    public void calculateRiskLevel() {
        if (riskScore < 25) {
            riskLevel = "LOW";
        } else if (riskScore < 50) {
            riskLevel = "MEDIUM";
        } else if (riskScore < 75) {
            riskLevel = "HIGH";
        } else {
            riskLevel = "CRITICAL";
        }
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getTokenizationRequestId() { return tokenizationRequestId; }
    public void setTokenizationRequestId(String tokenizationRequestId) { this.tokenizationRequestId = tokenizationRequestId; }
    
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }
    
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    
    public List<String> getTriggeredRules() { return triggeredRules; }
    public void setTriggeredRules(List<String> triggeredRules) { this.triggeredRules = triggeredRules; }
    
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getBrowserInfo() { return browserInfo; }
    public void setBrowserInfo(String browserInfo) { this.browserInfo = browserInfo; }
    
    public String getOsInfo() { return osInfo; }
    public void setOsInfo(String osInfo) { this.osInfo = osInfo; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getGeoCountry() { return geoCountry; }
    public void setGeoCountry(String geoCountry) { this.geoCountry = geoCountry; }
    
    public String getGeoCity() { return geoCity; }
    public void setGeoCity(String geoCity) { this.geoCity = geoCity; }
    
    public Double getGeoLatitude() { return geoLatitude; }
    public void setGeoLatitude(Double geoLatitude) { this.geoLatitude = geoLatitude; }
    
    public Double getGeoLongitude() { return geoLongitude; }
    public void setGeoLongitude(Double geoLongitude) { this.geoLongitude = geoLongitude; }
    
    public boolean isVpn() { return isVpn; }
    public void setVpn(boolean vpn) { isVpn = vpn; }
    
    public boolean isProxy() { return isProxy; }
    public void setProxy(boolean proxy) { isProxy = proxy; }
    
    public boolean isTor() { return isTor; }
    public void setTor(boolean tor) { isTor = tor; }
    
    public Long getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(Long sessionDuration) { this.sessionDuration = sessionDuration; }
    
    public Integer getPagesVisited() { return pagesVisited; }
    public void setPagesVisited(Integer pagesVisited) { this.pagesVisited = pagesVisited; }
    
    public Integer getMouseMovements() { return mouseMovements; }
    public void setMouseMovements(Integer mouseMovements) { this.mouseMovements = mouseMovements; }
    
    public Double getTypingSpeed() { return typingSpeed; }
    public void setTypingSpeed(Double typingSpeed) { this.typingSpeed = typingSpeed; }
    
    public Double getMlAnomalyScore() { return mlAnomalyScore; }
    public void setMlAnomalyScore(Double mlAnomalyScore) { this.mlAnomalyScore = mlAnomalyScore; }
    
    public Double getMlFraudProbability() { return mlFraudProbability; }
    public void setMlFraudProbability(Double mlFraudProbability) { this.mlFraudProbability = mlFraudProbability; }
    
    public String getMlModelVersion() { return mlModelVersion; }
    public void setMlModelVersion(String mlModelVersion) { this.mlModelVersion = mlModelVersion; }
    
    public Map<String, Double> getMlFeatureScores() { return mlFeatureScores; }
    public void setMlFeatureScores(Map<String, Double> mlFeatureScores) { this.mlFeatureScores = mlFeatureScores; }
    
    public Integer getTokensLastHour() { return tokensLastHour; }
    public void setTokensLastHour(Integer tokensLastHour) { this.tokensLastHour = tokensLastHour; }
    
    public Integer getTokensLastDay() { return tokensLastDay; }
    public void setTokensLastDay(Integer tokensLastDay) { this.tokensLastDay = tokensLastDay; }
    
    public Integer getUniqueCardsLastHour() { return uniqueCardsLastHour; }
    public void setUniqueCardsLastHour(Integer uniqueCardsLastHour) { this.uniqueCardsLastHour = uniqueCardsLastHour; }
    
    public Integer getFailedAttemptsLastHour() { return failedAttemptsLastHour; }
    public void setFailedAttemptsLastHour(Integer failedAttemptsLastHour) { this.failedAttemptsLastHour = failedAttemptsLastHour; }
    
    public String getAdditionalContext() { return additionalContext; }
    public void setAdditionalContext(String additionalContext) { this.additionalContext = additionalContext; }
    
    public String getManualReviewNotes() { return manualReviewNotes; }
    public void setManualReviewNotes(String manualReviewNotes) { this.manualReviewNotes = manualReviewNotes; }
    
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public Boolean getIsFalsePositive() { return isFalsePositive; }
    public void setIsFalsePositive(Boolean isFalsePositive) { this.isFalsePositive = isFalsePositive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}