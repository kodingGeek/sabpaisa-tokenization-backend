package com.sabpaisa.tokenization.biometric;

import com.sabpaisa.tokenization.biometric.BiometricComponents.*;
import com.sabpaisa.tokenization.biometric.BiometricDataStructures.*;
import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.domain.entity.Merchant;
import com.sabpaisa.tokenization.repository.BiometricEnrollmentRepository;
import com.sabpaisa.tokenization.repository.BiometricTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Advanced Biometric Tokenization Service
 * 
 * This service provides biometric-based tokenization using multiple modalities:
 * - Facial recognition templates
 * - Fingerprint minutiae
 * - Voice biometrics
 * - Behavioral patterns (typing, mouse movements)
 * - Multi-modal fusion for enhanced security
 */
@Service
public class BiometricTokenizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BiometricTokenizationService.class);
    
    private static final double FACIAL_MATCH_THRESHOLD = 0.95;
    private static final double FINGERPRINT_MATCH_THRESHOLD = 0.98;
    private static final double VOICE_MATCH_THRESHOLD = 0.90;
    private static final double BEHAVIORAL_MATCH_THRESHOLD = 0.85;
    private static final double MULTI_MODAL_THRESHOLD = 0.92;
    
    @Autowired
    private BiometricFusionEngine fusionEngine;
    
    @Autowired(required = false)
    private BiometricEnrollmentRepository enrollmentRepository;
    
    @Autowired(required = false)
    private BiometricTokenRepository tokenRepository;
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, BiometricProfile> biometricProfiles = new ConcurrentHashMap<>();
    private final Map<String, BiometricToken> biometricTokens = new ConcurrentHashMap<>();
    
    /**
     * Enroll biometric data and create a biometric profile
     */
    public BiometricEnrollmentResult enrollBiometrics(BiometricEnrollmentRequest request) {
        logger.info("Starting biometric enrollment for user: {}", request.getUserId());
        
        try {
            BiometricProfile profile = new BiometricProfile();
            profile.setUserId(request.getUserId());
            profile.setMerchantId(request.getMerchantId());
            profile.setEnrollmentId(generateEnrollmentId());
            profile.setCreatedAt(LocalDateTime.now());
            
            // Extract biometric templates
            if (request.getFacialData() != null) {
                FacialTemplate facial = extractFacialTemplate(request.getFacialData());
                profile.setFacialTemplate(facial);
                profile.getEnabledModalities().add("FACIAL");
            }
            
            if (request.getFingerprintData() != null) {
                FingerprintTemplate fingerprint = extractFingerprintTemplate(request.getFingerprintData());
                profile.setFingerprintTemplate(fingerprint);
                profile.getEnabledModalities().add("FINGERPRINT");
            }
            
            if (request.getVoiceData() != null) {
                VoiceTemplate voice = extractVoiceTemplate(request.getVoiceData());
                profile.setVoiceTemplate(voice);
                profile.getEnabledModalities().add("VOICE");
            }
            
            if (request.getBehavioralData() != null) {
                BehavioralTemplate behavioral = extractBehavioralTemplate(request.getBehavioralData());
                profile.setBehavioralTemplate(behavioral);
                profile.getEnabledModalities().add("BEHAVIORAL");
            }
            
            // Calculate biometric hash for privacy
            profile.setBiometricHash(calculateBiometricHash(profile));
            
            // Generate anti-spoofing challenge
            profile.setAntiSpoofingSecret(generateAntiSpoofingSecret());
            
            // Store profile
            biometricProfiles.put(profile.getEnrollmentId(), profile);
            
            // Create result
            BiometricEnrollmentResult result = new BiometricEnrollmentResult();
            result.setEnrollmentId(profile.getEnrollmentId());
            result.setSuccess(true);
            result.setEnabledModalities(profile.getEnabledModalities());
            result.setQualityScores(calculateQualityScores(profile));
            result.setSecurityLevel(calculateSecurityLevel(profile));
            
            logger.info("Biometric enrollment successful: {}", result.getEnrollmentId());
            return result;
            
        } catch (Exception e) {
            logger.error("Biometric enrollment failed", e);
            throw new RuntimeException("Biometric enrollment failed", e);
        }
    }
    
    /**
     * Create a biometric-protected token
     */
    public BiometricTokenResult createBiometricToken(BiometricTokenRequest request) {
        logger.info("Creating biometric token for enrollment: {}", request.getEnrollmentId());
        
        try {
            // Verify biometric authentication
            BiometricAuthResult authResult = authenticateBiometrics(request);
            if (!authResult.isAuthenticated()) {
                throw new SecurityException("Biometric authentication failed");
            }
            
            // Get biometric profile
            BiometricProfile profile = biometricProfiles.get(request.getEnrollmentId());
            if (profile == null) {
                throw new IllegalArgumentException("Biometric profile not found");
            }
            
            // Create biometric token
            BiometricToken token = new BiometricToken();
            token.setTokenId(generateBiometricTokenId());
            token.setEnrollmentId(request.getEnrollmentId());
            token.setCardHash(hashCard(request.getCardNumber()));
            token.setMaskedCard(maskCard(request.getCardNumber()));
            token.setCreatedAt(LocalDateTime.now());
            token.setExpiresAt(LocalDateTime.now().plusYears(3));
            
            // Add biometric binding
            token.setBiometricBinding(createBiometricBinding(profile, request));
            
            // Set authentication requirements
            token.setRequiredModalities(determineRequiredModalities(request.getSecurityLevel()));
            token.setMinConfidenceScore(determineMinConfidence(request.getSecurityLevel()));
            
            // Enable liveness detection
            token.setLivenessDetectionRequired(request.getSecurityLevel().equals("HIGH"));
            
            // Store token
            biometricTokens.put(token.getTokenId(), token);
            
            // Create result
            BiometricTokenResult result = new BiometricTokenResult();
            result.setTokenId(token.getTokenId());
            result.setMaskedCard(token.getMaskedCard());
            result.setBiometricBinding(token.getBiometricBinding());
            result.setAuthenticationScore(authResult.getConfidenceScore());
            result.setSecurityLevel(request.getSecurityLevel());
            
            logger.info("Biometric token created successfully: {}", token.getTokenId());
            return result;
            
        } catch (Exception e) {
            logger.error("Biometric token creation failed", e);
            throw new RuntimeException("Biometric token creation failed", e);
        }
    }
    
    /**
     * Authenticate using biometrics for token usage
     */
    public BiometricAuthResult authenticateBiometrics(BiometricTokenRequest request) {
        logger.info("Authenticating biometrics for enrollment: {}", request.getEnrollmentId());
        
        BiometricAuthResult result = new BiometricAuthResult();
        
        try {
            BiometricProfile profile = biometricProfiles.get(request.getEnrollmentId());
            if (profile == null) {
                result.setAuthenticated(false);
                result.setReason("Profile not found");
                return result;
            }
            
            // Perform liveness detection
            if (request.isLivenessCheckRequired()) {
                LivenessResult liveness = performLivenessDetection(request);
                if (!liveness.isLive()) {
                    result.setAuthenticated(false);
                    result.setReason("Liveness detection failed");
                    result.setSpoofingAttempt(true);
                    return result;
                }
            }
            
            // Match each biometric modality
            Map<String, Double> modalityScores = new HashMap<>();
            
            if (request.getFacialData() != null && profile.getFacialTemplate() != null) {
                double facialScore = matchFacialBiometric(
                    request.getFacialData(), 
                    profile.getFacialTemplate()
                );
                modalityScores.put("FACIAL", facialScore);
            }
            
            if (request.getFingerprintData() != null && profile.getFingerprintTemplate() != null) {
                double fingerprintScore = matchFingerprintBiometric(
                    request.getFingerprintData(),
                    profile.getFingerprintTemplate()
                );
                modalityScores.put("FINGERPRINT", fingerprintScore);
            }
            
            if (request.getVoiceData() != null && profile.getVoiceTemplate() != null) {
                double voiceScore = matchVoiceBiometric(
                    request.getVoiceData(),
                    profile.getVoiceTemplate()
                );
                modalityScores.put("VOICE", voiceScore);
            }
            
            if (request.getBehavioralData() != null && profile.getBehavioralTemplate() != null) {
                double behavioralScore = matchBehavioralBiometric(
                    request.getBehavioralData(),
                    profile.getBehavioralTemplate()
                );
                modalityScores.put("BEHAVIORAL", behavioralScore);
            }
            
            // Perform multi-modal fusion
            double fusedScore = fusionEngine.fuseScores(modalityScores);
            
            // Make authentication decision
            boolean authenticated = fusedScore >= MULTI_MODAL_THRESHOLD;
            
            result.setAuthenticated(authenticated);
            result.setConfidenceScore(fusedScore);
            result.setModalityScores(modalityScores);
            result.setUsedModalities(modalityScores.keySet());
            result.setTimestamp(LocalDateTime.now());
            
            // Update behavioral template with new data
            if (authenticated && request.getBehavioralData() != null) {
                updateBehavioralTemplate(profile, request.getBehavioralData());
            }
            
            logger.info("Biometric authentication result: {} (score: {})", 
                       authenticated, fusedScore);
            
        } catch (Exception e) {
            logger.error("Biometric authentication error", e);
            result.setAuthenticated(false);
            result.setReason("Authentication error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Retrieve token using biometric authentication
     */
    public BiometricTokenRetrievalResult retrieveTokenWithBiometrics(
            String tokenId, BiometricAuthData authData) {
        
        logger.info("Retrieving biometric token: {}", tokenId);
        
        BiometricTokenRetrievalResult result = new BiometricTokenRetrievalResult();
        
        try {
            // Get token
            BiometricToken token = biometricTokens.get(tokenId);
            if (token == null) {
                result.setSuccess(false);
                result.setReason("Token not found");
                return result;
            }
            
            // Create auth request
            BiometricTokenRequest authRequest = new BiometricTokenRequest();
            authRequest.setEnrollmentId(token.getEnrollmentId());
            authRequest.setFacialData(authData.getFacialData());
            authRequest.setFingerprintData(authData.getFingerprintData());
            authRequest.setVoiceData(authData.getVoiceData());
            authRequest.setBehavioralData(authData.getBehavioralData());
            authRequest.setLivenessCheckRequired(token.isLivenessDetectionRequired());
            
            // Authenticate
            BiometricAuthResult authResult = authenticateBiometrics(authRequest);
            
            if (!authResult.isAuthenticated()) {
                result.setSuccess(false);
                result.setReason("Biometric authentication failed");
                result.setAuthenticationScore(authResult.getConfidenceScore());
                
                // Log failed attempt
                logFailedAttempt(tokenId, authResult);
                
                return result;
            }
            
            // Verify required modalities
            Set<String> requiredModalities = token.getRequiredModalities();
            Set<String> usedModalities = authResult.getUsedModalities();
            
            if (!usedModalities.containsAll(requiredModalities)) {
                result.setSuccess(false);
                result.setReason("Missing required biometric modalities");
                return result;
            }
            
            // Check minimum confidence
            if (authResult.getConfidenceScore() < token.getMinConfidenceScore()) {
                result.setSuccess(false);
                result.setReason("Confidence score below threshold");
                return result;
            }
            
            // Success - return masked card data
            result.setSuccess(true);
            result.setMaskedCard(token.getMaskedCard());
            result.setAuthenticationScore(authResult.getConfidenceScore());
            result.setUsedModalities(authResult.getUsedModalities());
            
            // Update usage stats
            token.incrementUsageCount();
            token.setLastUsedAt(LocalDateTime.now());
            
            logger.info("Biometric token retrieved successfully");
            
        } catch (Exception e) {
            logger.error("Token retrieval error", e);
            result.setSuccess(false);
            result.setReason("Retrieval error: " + e.getMessage());
        }
        
        return result;
    }
    
    // Biometric extraction methods
    
    private FacialTemplate extractFacialTemplate(byte[] facialData) {
        // Simulate facial feature extraction
        FacialTemplate template = new FacialTemplate();
        template.setFeatureVector(extractFeatures(facialData, 512)); // 512-dimensional vector
        template.setLandmarks(extractFacialLandmarks(facialData));
        template.setQualityScore(calculateImageQuality(facialData));
        return template;
    }
    
    private FingerprintTemplate extractFingerprintTemplate(byte[] fingerprintData) {
        // Simulate fingerprint minutiae extraction
        FingerprintTemplate template = new FingerprintTemplate();
        template.setMinutiae(extractMinutiae(fingerprintData));
        template.setRidgeFlow(extractRidgeFlow(fingerprintData));
        template.setQualityScore(calculateFingerprintQuality(fingerprintData));
        return template;
    }
    
    private VoiceTemplate extractVoiceTemplate(byte[] voiceData) {
        // Simulate voice feature extraction
        VoiceTemplate template = new VoiceTemplate();
        template.setMfccFeatures(extractMFCC(voiceData));
        template.setPitchContour(extractPitch(voiceData));
        template.setFormants(extractFormants(voiceData));
        template.setQualityScore(calculateAudioQuality(voiceData));
        return template;
    }
    
    private BehavioralTemplate extractBehavioralTemplate(BehavioralData behavioralData) {
        // Extract behavioral patterns
        BehavioralTemplate template = new BehavioralTemplate();
        template.setTypingRhythm(BehavioralAnalyzer.analyzeTypingRhythm(behavioralData.getKeystrokeData()));
        template.setMouseDynamics(BehavioralAnalyzer.analyzeMouseMovements(behavioralData.getMouseData()));
        template.setInteractionPatterns(BehavioralAnalyzer.analyzeInteractionPatterns(behavioralData));
        return template;
    }
    
    // Biometric matching methods
    
    private double matchFacialBiometric(byte[] probeData, FacialTemplate enrolledTemplate) {
        FacialTemplate probeTemplate = extractFacialTemplate(probeData);
        
        // Cosine similarity of feature vectors
        double similarity = cosineSimilarity(
            probeTemplate.getFeatureVector(),
            enrolledTemplate.getFeatureVector()
        );
        
        // Adjust for quality
        double qualityFactor = (probeTemplate.getQualityScore() + enrolledTemplate.getQualityScore()) / 2.0;
        
        return similarity * qualityFactor;
    }
    
    private double matchFingerprintBiometric(byte[] probeData, FingerprintTemplate enrolledTemplate) {
        FingerprintTemplate probeTemplate = extractFingerprintTemplate(probeData);
        
        // Minutiae matching
        int matchedMinutiae = TemplateComparison.countMatchingMinutiae(
            probeTemplate.getMinutiae(),
            enrolledTemplate.getMinutiae()
        );
        
        double matchScore = (double) matchedMinutiae / enrolledTemplate.getMinutiae().size();
        
        return Math.min(matchScore * 1.2, 1.0); // Boost score but cap at 1.0
    }
    
    private double matchVoiceBiometric(byte[] probeData, VoiceTemplate enrolledTemplate) {
        VoiceTemplate probeTemplate = extractVoiceTemplate(probeData);
        
        // Dynamic Time Warping for MFCC comparison
        double mfccScore = TemplateComparison.dynamicTimeWarping(
            probeTemplate.getMfccFeatures(),
            enrolledTemplate.getMfccFeatures()
        );
        
        // Pitch comparison
        double pitchScore = TemplateComparison.comparePitchContours(
            probeTemplate.getPitchContour(),
            enrolledTemplate.getPitchContour()
        );
        
        return (mfccScore * 0.7 + pitchScore * 0.3);
    }
    
    private double matchBehavioralBiometric(BehavioralData probeData, BehavioralTemplate enrolledTemplate) {
        BehavioralTemplate probeTemplate = extractBehavioralTemplate(probeData);
        
        // Compare typing rhythm
        double typingScore = TemplateComparison.compareTypingRhythm(
            probeTemplate.getTypingRhythm(),
            enrolledTemplate.getTypingRhythm()
        );
        
        // Compare mouse dynamics
        double mouseScore = TemplateComparison.compareMouseDynamics(
            probeTemplate.getMouseDynamics(),
            enrolledTemplate.getMouseDynamics()
        );
        
        return (typingScore * 0.5 + mouseScore * 0.5);
    }
    
    // Liveness detection
    
    private LivenessResult performLivenessDetection(BiometricTokenRequest request) {
        LivenessResult result = new LivenessResult();
        
        // Check for facial liveness
        if (request.getFacialData() != null) {
            boolean facialLiveness = checkFacialLiveness(request.getFacialData());
            result.setFacialLiveness(facialLiveness);
        }
        
        // Check for fingerprint liveness
        if (request.getFingerprintData() != null) {
            boolean fingerprintLiveness = checkFingerprintLiveness(request.getFingerprintData());
            result.setFingerprintLiveness(fingerprintLiveness);
        }
        
        // Check challenge-response
        if (request.getChallengeResponse() != null) {
            boolean challengePassed = verifyChallengeResponse(request.getChallengeResponse());
            result.setChallengeResponseValid(challengePassed);
        }
        
        // Overall liveness decision
        result.setLive(result.isFacialLiveness() || result.isFingerprintLiveness());
        
        return result;
    }
    
    // Helper methods
    
    private String generateEnrollmentId() {
        return "BIO-" + System.currentTimeMillis() + "-" + secureRandom.nextInt(10000);
    }
    
    private String generateBiometricTokenId() {
        return "BTK-" + System.currentTimeMillis() + "-" + secureRandom.nextInt(10000);
    }
    
    private String generateAntiSpoofingSecret() {
        byte[] secret = new byte[32];
        secureRandom.nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }
    
    private String calculateBiometricHash(BiometricProfile profile) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            
            if (profile.getFacialTemplate() != null) {
                // Convert float[] to byte[]
                float[] features = profile.getFacialTemplate().getFeatureVector();
                if (features != null) {
                    ByteBuffer buffer = ByteBuffer.allocate(features.length * 4);
                    for (float f : features) {
                        buffer.putFloat(f);
                    }
                    digest.update(buffer.array());
                }
            }
            if (profile.getFingerprintTemplate() != null) {
                digest.update(ByteBuffer.allocate(4).putInt(
                    profile.getFingerprintTemplate().getMinutiae().size()).array());
            }
            
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
    
    private String hashCard(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            return Base64.getEncoder().encodeToString(
                digest.digest(cardNumber.getBytes())
            );
        } catch (Exception e) {
            throw new RuntimeException("Card hashing failed", e);
        }
    }
    
    private String maskCard(String cardNumber) {
        if (cardNumber.length() < 10) return cardNumber;
        return cardNumber.substring(0, 6) + "******" + 
               cardNumber.substring(cardNumber.length() - 4);
    }
    
    private Map<String, Double> calculateQualityScores(BiometricProfile profile) {
        Map<String, Double> scores = new HashMap<>();
        
        if (profile.getFacialTemplate() != null) {
            scores.put("FACIAL", profile.getFacialTemplate().getQualityScore());
        }
        if (profile.getFingerprintTemplate() != null) {
            scores.put("FINGERPRINT", profile.getFingerprintTemplate().getQualityScore());
        }
        if (profile.getVoiceTemplate() != null) {
            scores.put("VOICE", profile.getVoiceTemplate().getQualityScore());
        }
        
        return scores;
    }
    
    private String calculateSecurityLevel(BiometricProfile profile) {
        int modalityCount = profile.getEnabledModalities().size();
        
        if (modalityCount >= 3) return "VERY_HIGH";
        if (modalityCount == 2) return "HIGH";
        return "MEDIUM";
    }
    
    private String createBiometricBinding(BiometricProfile profile, BiometricTokenRequest request) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                profile.getAntiSpoofingSecret().getBytes(), "HmacSHA256"
            );
            mac.init(secretKey);
            
            String data = profile.getBiometricHash() + ":" + request.getCardNumber();
            byte[] hmac = mac.doFinal(data.getBytes());
            
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Binding creation failed", e);
        }
    }
    
    private Set<String> determineRequiredModalities(String securityLevel) {
        Set<String> required = new HashSet<>();
        
        switch (securityLevel) {
            case "VERY_HIGH":
                required.add("FACIAL");
                required.add("FINGERPRINT");
                break;
            case "HIGH":
                required.add("FINGERPRINT");
                break;
            default:
                required.add("FACIAL");
        }
        
        return required;
    }
    
    private double determineMinConfidence(String securityLevel) {
        switch (securityLevel) {
            case "VERY_HIGH": return 0.95;
            case "HIGH": return 0.90;
            default: return 0.85;
        }
    }
    
    private void updateBehavioralTemplate(BiometricProfile profile, BehavioralData newData) {
        // Adaptive template update
        if (profile.getBehavioralTemplate() != null) {
            BehavioralTemplate newTemplate = extractBehavioralTemplate(newData);
            // Merge with existing template (simplified)
            profile.getBehavioralTemplate().updateWithNewData(newTemplate);
        }
    }
    
    private void logFailedAttempt(String tokenId, BiometricAuthResult authResult) {
        logger.warn("Failed biometric auth attempt for token: {}, score: {}, reason: {}",
                   tokenId, authResult.getConfidenceScore(), authResult.getReason());
        // In production, implement rate limiting and alert on multiple failures
    }
    
    // Simplified biometric processing methods (in production, use specialized libraries)
    
    private float[] extractFeatures(byte[] data, int dimensions) {
        float[] features = new float[dimensions];
        for (int i = 0; i < dimensions; i++) {
            features[i] = (float) Math.random(); // Simulated
        }
        return features;
    }
    
    private List<FacialLandmark> extractFacialLandmarks(byte[] data) {
        // Simulate 68 facial landmarks
        List<FacialLandmark> landmarks = new ArrayList<>();
        for (int i = 0; i < 68; i++) {
            landmarks.add(new FacialLandmark(i, Math.random() * 100, Math.random() * 100));
        }
        return landmarks;
    }
    
    private List<Minutia> extractMinutiae(byte[] data) {
        // Simulate minutiae extraction
        List<Minutia> minutiae = new ArrayList<>();
        int count = 30 + secureRandom.nextInt(20);
        for (int i = 0; i < count; i++) {
            minutiae.add(new Minutia(
                Math.random() * 300, // x
                Math.random() * 400, // y
                Math.random() * 360  // angle
            ));
        }
        return minutiae;
    }
    
    private double[][] extractMFCC(byte[] audioData) {
        // Simulate MFCC extraction (13 coefficients x N frames)
        int frames = 100;
        double[][] mfcc = new double[frames][13];
        for (int i = 0; i < frames; i++) {
            for (int j = 0; j < 13; j++) {
                mfcc[i][j] = Math.random();
            }
        }
        return mfcc;
    }
    
    private double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    // Additional helper methods would be implemented here...
    
    // Inner classes for biometric data structures
    // These are temporary - using the ones from BiometricDataStructures in production
    
    public static class BiometricProfile {
        private String userId;
        private String merchantId;
        private String enrollmentId;
        private LocalDateTime createdAt;
        private FacialTemplate facialTemplate;
        private FingerprintTemplate fingerprintTemplate;
        private VoiceTemplate voiceTemplate;
        private BehavioralTemplate behavioralTemplate;
        private Set<String> enabledModalities = new HashSet<>();
        private String biometricHash;
        private String antiSpoofingSecret;
        
        // Getters and setters omitted for brevity
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        
        public String getEnrollmentId() { return enrollmentId; }
        public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public FacialTemplate getFacialTemplate() { return facialTemplate; }
        public void setFacialTemplate(FacialTemplate facialTemplate) { this.facialTemplate = facialTemplate; }
        
        public FingerprintTemplate getFingerprintTemplate() { return fingerprintTemplate; }
        public void setFingerprintTemplate(FingerprintTemplate fingerprintTemplate) { this.fingerprintTemplate = fingerprintTemplate; }
        
        public VoiceTemplate getVoiceTemplate() { return voiceTemplate; }
        public void setVoiceTemplate(VoiceTemplate voiceTemplate) { this.voiceTemplate = voiceTemplate; }
        
        public BehavioralTemplate getBehavioralTemplate() { return behavioralTemplate; }
        public void setBehavioralTemplate(BehavioralTemplate behavioralTemplate) { this.behavioralTemplate = behavioralTemplate; }
        
        public Set<String> getEnabledModalities() { return enabledModalities; }
        public void setEnabledModalities(Set<String> enabledModalities) { this.enabledModalities = enabledModalities; }
        
        public String getBiometricHash() { return biometricHash; }
        public void setBiometricHash(String biometricHash) { this.biometricHash = biometricHash; }
        
        public String getAntiSpoofingSecret() { return antiSpoofingSecret; }
        public void setAntiSpoofingSecret(String antiSpoofingSecret) { this.antiSpoofingSecret = antiSpoofingSecret; }
    }
    
    // Using simplified local template classes for now
    public static class FacialTemplate {
        private float[] featureVector;
        private List<FacialLandmark> landmarks;
        private double qualityScore;
        
        public float[] getFeatureVector() { return featureVector; }
        public void setFeatureVector(float[] featureVector) { this.featureVector = featureVector; }
        
        public List<FacialLandmark> getLandmarks() { return landmarks; }
        public void setLandmarks(List<FacialLandmark> landmarks) { this.landmarks = landmarks; }
        
        public double getQualityScore() { return qualityScore; }
        public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
    }
    
    public static class FingerprintTemplate {
        private List<Minutia> minutiae;
        private double[][] ridgeFlow;
        private double qualityScore;
        
        public List<Minutia> getMinutiae() { return minutiae; }
        public void setMinutiae(List<Minutia> minutiae) { this.minutiae = minutiae; }
        
        public double[][] getRidgeFlow() { return ridgeFlow; }
        public void setRidgeFlow(double[][] ridgeFlow) { this.ridgeFlow = ridgeFlow; }
        
        public double getQualityScore() { return qualityScore; }
        public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
    }
    
    // Additional template classes and methods...
    
    // Placeholder methods for simulation
    private double calculateImageQuality(byte[] data) { 
        return QualityAssessment.assessFacialImageQuality(data);
    }
    private double[][] extractRidgeFlow(byte[] data) { return new double[10][10]; }
    private double calculateFingerprintQuality(byte[] data) { 
        return QualityAssessment.assessFingerprintQuality(data);
    }
    private double[] extractPitch(byte[] data) { return new double[100]; }
    private double[][] extractFormants(byte[] data) { return new double[100][4]; }
    private double calculateAudioQuality(byte[] data) { 
        return QualityAssessment.assessVoiceQuality(data);
    }
    
    // Helper methods
    
    // Additional placeholder implementations...
    
    private boolean verifyChallengeResponse(String response) {
        // In production, would verify actual challenge-response
        return response != null && response.length() > 10;
    }
    
    private boolean checkFacialLiveness(byte[] facialData) {
        // Check for facial spoofing
        Map<String, Object> metadata = new HashMap<>();
        return !AntiSpoofingDetector.detectFacialSpoofing(facialData, metadata);
    }
    
    private boolean checkFingerprintLiveness(byte[] fingerprintData) {
        // Check for fingerprint spoofing
        Map<String, Object> metadata = new HashMap<>();
        return !AntiSpoofingDetector.detectFingerprintSpoofing(fingerprintData, metadata);
    }
}