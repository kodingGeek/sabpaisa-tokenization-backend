package com.sabpaisa.tokenization.biometric;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Data structures for biometric tokenization
 */
public class BiometricDataStructures {
    
    // Request/Response classes
    
    public static class BiometricEnrollmentRequest {
        private String userId;
        private String merchantId;
        private byte[] facialData;
        private byte[] fingerprintData;
        private byte[] voiceData;
        private BehavioralData behavioralData;
        private Map<String, String> metadata;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        
        public byte[] getFacialData() { return facialData; }
        public void setFacialData(byte[] facialData) { this.facialData = facialData; }
        
        public byte[] getFingerprintData() { return fingerprintData; }
        public void setFingerprintData(byte[] fingerprintData) { this.fingerprintData = fingerprintData; }
        
        public byte[] getVoiceData() { return voiceData; }
        public void setVoiceData(byte[] voiceData) { this.voiceData = voiceData; }
        
        public BehavioralData getBehavioralData() { return behavioralData; }
        public void setBehavioralData(BehavioralData behavioralData) { this.behavioralData = behavioralData; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }
    
    public static class BiometricEnrollmentResult {
        private String enrollmentId;
        private boolean success;
        private Set<String> enabledModalities;
        private Map<String, Double> qualityScores;
        private String securityLevel;
        private String message;
        
        // Getters and setters
        public String getEnrollmentId() { return enrollmentId; }
        public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Set<String> getEnabledModalities() { return enabledModalities; }
        public void setEnabledModalities(Set<String> enabledModalities) { this.enabledModalities = enabledModalities; }
        
        public Map<String, Double> getQualityScores() { return qualityScores; }
        public void setQualityScores(Map<String, Double> qualityScores) { this.qualityScores = qualityScores; }
        
        public String getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class BiometricTokenRequest {
        private String enrollmentId;
        private String cardNumber;
        private String securityLevel; // LOW, MEDIUM, HIGH, VERY_HIGH
        private byte[] facialData;
        private byte[] fingerprintData;
        private byte[] voiceData;
        private BehavioralData behavioralData;
        private boolean livenessCheckRequired;
        private String challengeResponse;
        
        // Getters and setters
        public String getEnrollmentId() { return enrollmentId; }
        public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
        
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        
        public String getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
        
        public byte[] getFacialData() { return facialData; }
        public void setFacialData(byte[] facialData) { this.facialData = facialData; }
        
        public byte[] getFingerprintData() { return fingerprintData; }
        public void setFingerprintData(byte[] fingerprintData) { this.fingerprintData = fingerprintData; }
        
        public byte[] getVoiceData() { return voiceData; }
        public void setVoiceData(byte[] voiceData) { this.voiceData = voiceData; }
        
        public BehavioralData getBehavioralData() { return behavioralData; }
        public void setBehavioralData(BehavioralData behavioralData) { this.behavioralData = behavioralData; }
        
        public boolean isLivenessCheckRequired() { return livenessCheckRequired; }
        public void setLivenessCheckRequired(boolean livenessCheckRequired) { this.livenessCheckRequired = livenessCheckRequired; }
        
        public String getChallengeResponse() { return challengeResponse; }
        public void setChallengeResponse(String challengeResponse) { this.challengeResponse = challengeResponse; }
    }
    
    public static class BiometricTokenResult {
        private String tokenId;
        private String maskedCard;
        private String biometricBinding;
        private double authenticationScore;
        private String securityLevel;
        private LocalDateTime expiresAt;
        
        // Getters and setters
        public String getTokenId() { return tokenId; }
        public void setTokenId(String tokenId) { this.tokenId = tokenId; }
        
        public String getMaskedCard() { return maskedCard; }
        public void setMaskedCard(String maskedCard) { this.maskedCard = maskedCard; }
        
        public String getBiometricBinding() { return biometricBinding; }
        public void setBiometricBinding(String biometricBinding) { this.biometricBinding = biometricBinding; }
        
        public double getAuthenticationScore() { return authenticationScore; }
        public void setAuthenticationScore(double authenticationScore) { this.authenticationScore = authenticationScore; }
        
        public String getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
        
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }
    
    public static class BiometricAuthResult {
        private boolean authenticated;
        private double confidenceScore;
        private Map<String, Double> modalityScores;
        private Set<String> usedModalities;
        private String reason;
        private boolean spoofingAttempt;
        private LocalDateTime timestamp;
        
        // Getters and setters
        public boolean isAuthenticated() { return authenticated; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
        
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
        
        public Map<String, Double> getModalityScores() { return modalityScores; }
        public void setModalityScores(Map<String, Double> modalityScores) { this.modalityScores = modalityScores; }
        
        public Set<String> getUsedModalities() { return usedModalities; }
        public void setUsedModalities(Set<String> usedModalities) { this.usedModalities = usedModalities; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public boolean isSpoofingAttempt() { return spoofingAttempt; }
        public void setSpoofingAttempt(boolean spoofingAttempt) { this.spoofingAttempt = spoofingAttempt; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class BiometricAuthData {
        private byte[] facialData;
        private byte[] fingerprintData;
        private byte[] voiceData;
        private BehavioralData behavioralData;
        
        // Getters and setters
        public byte[] getFacialData() { return facialData; }
        public void setFacialData(byte[] facialData) { this.facialData = facialData; }
        
        public byte[] getFingerprintData() { return fingerprintData; }
        public void setFingerprintData(byte[] fingerprintData) { this.fingerprintData = fingerprintData; }
        
        public byte[] getVoiceData() { return voiceData; }
        public void setVoiceData(byte[] voiceData) { this.voiceData = voiceData; }
        
        public BehavioralData getBehavioralData() { return behavioralData; }
        public void setBehavioralData(BehavioralData behavioralData) { this.behavioralData = behavioralData; }
    }
    
    public static class BiometricTokenRetrievalResult {
        private boolean success;
        private String maskedCard;
        private double authenticationScore;
        private Set<String> usedModalities;
        private String reason;
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMaskedCard() { return maskedCard; }
        public void setMaskedCard(String maskedCard) { this.maskedCard = maskedCard; }
        
        public double getAuthenticationScore() { return authenticationScore; }
        public void setAuthenticationScore(double authenticationScore) { this.authenticationScore = authenticationScore; }
        
        public Set<String> getUsedModalities() { return usedModalities; }
        public void setUsedModalities(Set<String> usedModalities) { this.usedModalities = usedModalities; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    // Biometric data structures
    
    public static class BiometricToken {
        private String tokenId;
        private String enrollmentId;
        private String cardHash;
        private String maskedCard;
        private String biometricBinding;
        private Set<String> requiredModalities;
        private double minConfidenceScore;
        private boolean livenessDetectionRequired;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime lastUsedAt;
        private int usageCount;
        
        public void incrementUsageCount() {
            this.usageCount++;
        }
        
        // Getters and setters
        public String getTokenId() { return tokenId; }
        public void setTokenId(String tokenId) { this.tokenId = tokenId; }
        
        public String getEnrollmentId() { return enrollmentId; }
        public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
        
        public String getCardHash() { return cardHash; }
        public void setCardHash(String cardHash) { this.cardHash = cardHash; }
        
        public String getMaskedCard() { return maskedCard; }
        public void setMaskedCard(String maskedCard) { this.maskedCard = maskedCard; }
        
        public String getBiometricBinding() { return biometricBinding; }
        public void setBiometricBinding(String biometricBinding) { this.biometricBinding = biometricBinding; }
        
        public Set<String> getRequiredModalities() { return requiredModalities; }
        public void setRequiredModalities(Set<String> requiredModalities) { this.requiredModalities = requiredModalities; }
        
        public double getMinConfidenceScore() { return minConfidenceScore; }
        public void setMinConfidenceScore(double minConfidenceScore) { this.minConfidenceScore = minConfidenceScore; }
        
        public boolean isLivenessDetectionRequired() { return livenessDetectionRequired; }
        public void setLivenessDetectionRequired(boolean livenessDetectionRequired) { this.livenessDetectionRequired = livenessDetectionRequired; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        
        public LocalDateTime getLastUsedAt() { return lastUsedAt; }
        public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
        
        public int getUsageCount() { return usageCount; }
        public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
    }
    
    public static class BehavioralData {
        private List<KeystrokeEvent> keystrokeData;
        private List<MouseEvent> mouseData;
        private List<TouchEvent> touchData;
        private Map<String, Object> deviceInfo;
        private long sessionDuration;
        
        // Getters and setters
        public List<KeystrokeEvent> getKeystrokeData() { return keystrokeData; }
        public void setKeystrokeData(List<KeystrokeEvent> keystrokeData) { this.keystrokeData = keystrokeData; }
        
        public List<MouseEvent> getMouseData() { return mouseData; }
        public void setMouseData(List<MouseEvent> mouseData) { this.mouseData = mouseData; }
        
        public List<TouchEvent> getTouchData() { return touchData; }
        public void setTouchData(List<TouchEvent> touchData) { this.touchData = touchData; }
        
        public Map<String, Object> getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(Map<String, Object> deviceInfo) { this.deviceInfo = deviceInfo; }
        
        public long getSessionDuration() { return sessionDuration; }
        public void setSessionDuration(long sessionDuration) { this.sessionDuration = sessionDuration; }
    }
    
    public static class VoiceTemplate {
        private double[][] mfccFeatures;
        private double[] pitchContour;
        private double[][] formants;
        private double qualityScore;
        
        // Getters and setters
        public double[][] getMfccFeatures() { return mfccFeatures; }
        public void setMfccFeatures(double[][] mfccFeatures) { this.mfccFeatures = mfccFeatures; }
        
        public double[] getPitchContour() { return pitchContour; }
        public void setPitchContour(double[] pitchContour) { this.pitchContour = pitchContour; }
        
        public double[][] getFormants() { return formants; }
        public void setFormants(double[][] formants) { this.formants = formants; }
        
        public double getQualityScore() { return qualityScore; }
        public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
    }
    
    public static class BehavioralTemplate {
        private TypingRhythm typingRhythm;
        private MouseDynamics mouseDynamics;
        private Map<String, Object> interactionPatterns;
        
        public void updateWithNewData(BehavioralTemplate newTemplate) {
            // Adaptive update logic
            if (typingRhythm != null && newTemplate.typingRhythm != null) {
                typingRhythm.merge(newTemplate.typingRhythm);
            }
            if (mouseDynamics != null && newTemplate.mouseDynamics != null) {
                mouseDynamics.merge(newTemplate.mouseDynamics);
            }
        }
        
        // Getters and setters
        public TypingRhythm getTypingRhythm() { return typingRhythm; }
        public void setTypingRhythm(TypingRhythm typingRhythm) { this.typingRhythm = typingRhythm; }
        
        public MouseDynamics getMouseDynamics() { return mouseDynamics; }
        public void setMouseDynamics(MouseDynamics mouseDynamics) { this.mouseDynamics = mouseDynamics; }
        
        public Map<String, Object> getInteractionPatterns() { return interactionPatterns; }
        public void setInteractionPatterns(Map<String, Object> interactionPatterns) { this.interactionPatterns = interactionPatterns; }
    }
    
    // Supporting data structures
    
    public static class FacialLandmark {
        private int id;
        private double x;
        private double y;
        
        public FacialLandmark(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
        
        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
    }
    
    public static class Minutia {
        private double x;
        private double y;
        private double angle;
        private String type; // RIDGE_ENDING, BIFURCATION
        
        public Minutia(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.type = Math.random() > 0.5 ? "RIDGE_ENDING" : "BIFURCATION";
        }
        
        // Getters and setters
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        
        public double getAngle() { return angle; }
        public void setAngle(double angle) { this.angle = angle; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
    
    public static class KeystrokeEvent {
        private String key;
        private long timestamp;
        private long pressDuration;
        private long flightTime; // Time between keystrokes
        
        // Getters and setters
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public long getPressDuration() { return pressDuration; }
        public void setPressDuration(long pressDuration) { this.pressDuration = pressDuration; }
        
        public long getFlightTime() { return flightTime; }
        public void setFlightTime(long flightTime) { this.flightTime = flightTime; }
    }
    
    public static class MouseEvent {
        private double x;
        private double y;
        private long timestamp;
        private String eventType; // MOVE, CLICK, DRAG
        private double velocity;
        private double acceleration;
        
        // Getters and setters
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public double getVelocity() { return velocity; }
        public void setVelocity(double velocity) { this.velocity = velocity; }
        
        public double getAcceleration() { return acceleration; }
        public void setAcceleration(double acceleration) { this.acceleration = acceleration; }
    }
    
    public static class TouchEvent {
        private double x;
        private double y;
        private double pressure;
        private double area;
        private long timestamp;
        private String eventType; // TOUCH_DOWN, TOUCH_MOVE, TOUCH_UP
        
        // Getters and setters
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        
        public double getPressure() { return pressure; }
        public void setPressure(double pressure) { this.pressure = pressure; }
        
        public double getArea() { return area; }
        public void setArea(double area) { this.area = area; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
    
    public static class TypingRhythm {
        private Map<String, Double> digramTimes; // Average time between key pairs
        private double averageSpeed;
        private double speedVariation;
        private List<Double> dwellTimes; // Key press durations
        
        public void merge(TypingRhythm other) {
            // Merge typing patterns with weighted average
            for (Map.Entry<String, Double> entry : other.digramTimes.entrySet()) {
                digramTimes.merge(entry.getKey(), entry.getValue(), 
                    (old, new_) -> (old * 0.7 + new_ * 0.3)); // 70% old, 30% new
            }
            averageSpeed = averageSpeed * 0.7 + other.averageSpeed * 0.3;
        }
        
        // Getters and setters
        public Map<String, Double> getDigramTimes() { return digramTimes; }
        public void setDigramTimes(Map<String, Double> digramTimes) { this.digramTimes = digramTimes; }
        
        public double getAverageSpeed() { return averageSpeed; }
        public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }
        
        public double getSpeedVariation() { return speedVariation; }
        public void setSpeedVariation(double speedVariation) { this.speedVariation = speedVariation; }
        
        public List<Double> getDwellTimes() { return dwellTimes; }
        public void setDwellTimes(List<Double> dwellTimes) { this.dwellTimes = dwellTimes; }
    }
    
    public static class MouseDynamics {
        private double averageVelocity;
        private double averageAcceleration;
        private List<Double> clickDurations;
        private Map<String, Integer> movementPatterns;
        
        public void merge(MouseDynamics other) {
            averageVelocity = averageVelocity * 0.7 + other.averageVelocity * 0.3;
            averageAcceleration = averageAcceleration * 0.7 + other.averageAcceleration * 0.3;
        }
        
        // Getters and setters
        public double getAverageVelocity() { return averageVelocity; }
        public void setAverageVelocity(double averageVelocity) { this.averageVelocity = averageVelocity; }
        
        public double getAverageAcceleration() { return averageAcceleration; }
        public void setAverageAcceleration(double averageAcceleration) { this.averageAcceleration = averageAcceleration; }
        
        public List<Double> getClickDurations() { return clickDurations; }
        public void setClickDurations(List<Double> clickDurations) { this.clickDurations = clickDurations; }
        
        public Map<String, Integer> getMovementPatterns() { return movementPatterns; }
        public void setMovementPatterns(Map<String, Integer> movementPatterns) { this.movementPatterns = movementPatterns; }
    }
    
    public static class LivenessResult {
        private boolean live;
        private boolean facialLiveness;
        private boolean fingerprintLiveness;
        private boolean challengeResponseValid;
        private double confidenceScore;
        
        // Getters and setters
        public boolean isLive() { return live; }
        public void setLive(boolean live) { this.live = live; }
        
        public boolean isFacialLiveness() { return facialLiveness; }
        public void setFacialLiveness(boolean facialLiveness) { this.facialLiveness = facialLiveness; }
        
        public boolean isFingerprintLiveness() { return fingerprintLiveness; }
        public void setFingerprintLiveness(boolean fingerprintLiveness) { this.fingerprintLiveness = fingerprintLiveness; }
        
        public boolean isChallengeResponseValid() { return challengeResponseValid; }
        public void setChallengeResponseValid(boolean challengeResponseValid) { this.challengeResponseValid = challengeResponseValid; }
        
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    }
}