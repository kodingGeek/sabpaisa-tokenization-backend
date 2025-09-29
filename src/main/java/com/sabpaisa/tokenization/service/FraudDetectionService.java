package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.entity.*;
import com.sabpaisa.tokenization.dto.TokenizationRequest;
import com.sabpaisa.tokenization.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FraudDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);
    
    @Autowired
    private FraudDetectionRuleRepository ruleRepository;
    
    @Autowired
    private FraudDetectionEventRepository eventRepository;
    
    @Autowired
    private TokenRepository tokenRepository;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    private static final String IP_GEOLOCATION_API = "http://ip-api.com/json/";
    private static final String DEVICE_FINGERPRINT_API = "https://api.fingerprint.com/v1/";
    
    @Transactional
    public FraudDetectionResult evaluateFraudRisk(TokenizationRequest request, Map<String, String> headers) {
        logger.info("Starting fraud detection for merchant: {}", request.getMerchantId());
        
        FraudDetectionEvent event = new FraudDetectionEvent();
        event.setMerchantId(request.getMerchantId());
        event.setEventType("TOKENIZATION");
        event.setTokenizationRequestId(UUID.randomUUID().toString());
        
        // Collect device and location information
        CompletableFuture<Void> deviceInfoFuture = collectDeviceInformation(event, headers);
        CompletableFuture<Void> locationInfoFuture = collectLocationInformation(event, headers);
        
        // Run velocity checks
        performVelocityChecks(event, request);
        
        // Get active fraud rules
        List<FraudDetectionRule> activeRules = ruleRepository.findByActiveTrue()
            .stream()
            .sorted(Comparator.comparing(FraudDetectionRule::getPriority).reversed())
            .collect(Collectors.toList());
        
        // Evaluate each rule
        int totalRiskScore = 0;
        List<String> triggeredRules = new ArrayList<>();
        Map<String, Integer> ruleScores = new HashMap<>();
        
        for (FraudDetectionRule rule : activeRules) {
            if (evaluateRule(rule, event, request)) {
                triggeredRules.add(rule.getRuleName());
                ruleScores.put(rule.getRuleName(), rule.getRiskScore());
                totalRiskScore += rule.getRiskScore();
                
                // Update rule statistics
                rule.setTotalTriggers(rule.getTotalTriggers() + 1);
            }
            rule.setTotalEvaluations(rule.getTotalEvaluations() + 1);
        }
        
        // Run ML-based anomaly detection
        CompletableFuture<MLPrediction> mlPredictionFuture = runMLAnomalyDetection(event, request);
        
        // Wait for async operations to complete
        CompletableFuture.allOf(deviceInfoFuture, locationInfoFuture, mlPredictionFuture).join();
        
        // Add ML score to total risk
        MLPrediction mlPrediction = mlPredictionFuture.join();
        if (mlPrediction != null) {
            totalRiskScore += (int)(mlPrediction.getFraudProbability() * 50); // ML contributes up to 50 points
            event.setMlAnomalyScore(mlPrediction.getAnomalyScore());
            event.setMlFraudProbability(mlPrediction.getFraudProbability());
            event.setMlModelVersion(mlPrediction.getModelVersion());
            event.setMlFeatureScores(mlPrediction.getFeatureScores());
        }
        
        // Calculate final risk score (capped at 100)
        event.setRiskScore(Math.min(totalRiskScore, 100));
        event.calculateRiskLevel();
        event.setTriggeredRules(triggeredRules);
        
        // Make decision based on risk score
        String decision = makeDecision(event.getRiskScore(), event.getRiskLevel());
        event.setDecision(decision);
        
        // Save event
        event = eventRepository.save(event);
        
        // Send notifications for high-risk events
        if (event.getRiskScore() > 70) {
            sendHighRiskAlert(event, request);
        }
        
        // Create audit log
        auditLogService.logFraudCheck(event, request);
        
        return new FraudDetectionResult(
            event.getEventId(),
            event.getRiskScore(),
            event.getRiskLevel(),
            event.getDecision(),
            triggeredRules,
            ruleScores,
            event.getDeviceFingerprint()
        );
    }
    
    private boolean evaluateRule(FraudDetectionRule rule, FraudDetectionEvent event, TokenizationRequest request) {
        try {
            switch (rule.getRuleType()) {
                case "VELOCITY":
                    return evaluateVelocityRule(rule, event);
                case "GEO_LOCATION":
                    return evaluateGeoLocationRule(rule, event);
                case "AMOUNT_THRESHOLD":
                    return evaluateAmountThresholdRule(rule, request);
                case "DEVICE_FINGERPRINT":
                    return evaluateDeviceFingerprintRule(rule, event);
                case "ML_BASED":
                    return evaluateMLBasedRule(rule, event);
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error evaluating rule: {}", rule.getRuleName(), e);
            return false;
        }
    }
    
    private boolean evaluateVelocityRule(FraudDetectionRule rule, FraudDetectionEvent event) {
        Map<String, String> params = rule.getParameters();
        
        if (params.containsKey("maxTokensPerHour")) {
            int maxAllowed = Integer.parseInt(params.get("maxTokensPerHour"));
            if (event.getTokensLastHour() > maxAllowed) {
                return true;
            }
        }
        
        if (params.containsKey("maxUniqueCardsPerHour")) {
            int maxAllowed = Integer.parseInt(params.get("maxUniqueCardsPerHour"));
            if (event.getUniqueCardsLastHour() > maxAllowed) {
                return true;
            }
        }
        
        if (params.containsKey("maxFailedAttemptsPerHour")) {
            int maxAllowed = Integer.parseInt(params.get("maxFailedAttemptsPerHour"));
            if (event.getFailedAttemptsLastHour() > maxAllowed) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean evaluateGeoLocationRule(FraudDetectionRule rule, FraudDetectionEvent event) {
        Map<String, String> params = rule.getParameters();
        
        // Check if using VPN/Proxy/Tor
        if (params.containsKey("blockVPN") && "true".equals(params.get("blockVPN")) && event.isVpn()) {
            return true;
        }
        
        if (params.containsKey("blockProxy") && "true".equals(params.get("blockProxy")) && event.isProxy()) {
            return true;
        }
        
        if (params.containsKey("blockTor") && "true".equals(params.get("blockTor")) && event.isTor()) {
            return true;
        }
        
        // Check if from blocked countries
        if (params.containsKey("blockedCountries")) {
            List<String> blockedCountries = Arrays.asList(params.get("blockedCountries").split(","));
            if (event.getGeoCountry() != null && blockedCountries.contains(event.getGeoCountry())) {
                return true;
            }
        }
        
        // Check for impossible travel (location changed too quickly)
        if (params.containsKey("checkImpossibleTravel") && "true".equals(params.get("checkImpossibleTravel"))) {
            // Get last known location for this merchant
            Optional<FraudDetectionEvent> lastEvent = eventRepository.findLastEventByMerchantId(
                event.getMerchantId(), LocalDateTime.now().minusHours(1)
            );
            
            if (lastEvent.isPresent() && lastEvent.get().getGeoLatitude() != null && event.getGeoLatitude() != null) {
                double distance = calculateDistance(
                    lastEvent.get().getGeoLatitude(), lastEvent.get().getGeoLongitude(),
                    event.getGeoLatitude(), event.getGeoLongitude()
                );
                
                // If traveled more than 1000km in less than 1 hour, it's suspicious
                if (distance > 1000) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean evaluateAmountThresholdRule(FraudDetectionRule rule, TokenizationRequest request) {
        // This could be extended to check transaction amounts if applicable
        return false;
    }
    
    private boolean evaluateDeviceFingerprintRule(FraudDetectionRule rule, FraudDetectionEvent event) {
        if (event.getDeviceFingerprint() == null) {
            return false;
        }
        
        Map<String, String> params = rule.getParameters();
        
        // Check if device has been seen in fraudulent activities
        if (params.containsKey("checkBlacklistedDevices") && "true".equals(params.get("checkBlacklistedDevices"))) {
            List<FraudDetectionEvent> previousFraudEvents = eventRepository.findByDeviceFingerprintAndIsFalsePositiveFalse(
                event.getDeviceFingerprint()
            );
            
            if (!previousFraudEvents.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean evaluateMLBasedRule(FraudDetectionRule rule, FraudDetectionEvent event) {
        Map<String, String> params = rule.getParameters();
        
        if (event.getMlFraudProbability() != null) {
            double threshold = Double.parseDouble(params.getOrDefault("fraudProbabilityThreshold", "0.7"));
            return event.getMlFraudProbability() > threshold;
        }
        
        return false;
    }
    
    private void performVelocityChecks(FraudDetectionEvent event, TokenizationRequest request) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        // Count tokens created in last hour
        event.setTokensLastHour(tokenRepository.countByMerchantIdAndCreatedAtAfter(
            request.getMerchantId(), oneHourAgo
        ));
        
        // Count tokens created in last day
        event.setTokensLastDay(tokenRepository.countByMerchantIdAndCreatedAtAfter(
            request.getMerchantId(), oneDayAgo
        ));
        
        // Count unique cards tokenized in last hour
        event.setUniqueCardsLastHour(tokenRepository.countUniqueCardsByMerchantIdAndCreatedAtAfter(
            request.getMerchantId(), oneHourAgo
        ));
        
        // Count failed attempts in last hour
        event.setFailedAttemptsLastHour(eventRepository.countFailedAttemptsByMerchantIdAndCreatedAtAfter(
            request.getMerchantId(), oneHourAgo
        ));
    }
    
    private CompletableFuture<Void> collectDeviceInformation(FraudDetectionEvent event, Map<String, String> headers) {
        return CompletableFuture.runAsync(() -> {
            // Extract device information from headers
            event.setBrowserInfo(headers.getOrDefault("User-Agent", "Unknown"));
            event.setDeviceFingerprint(headers.getOrDefault("X-Device-Fingerprint", generateDeviceFingerprint(headers)));
            
            // Parse user agent for device type and OS
            String userAgent = headers.getOrDefault("User-Agent", "");
            if (userAgent.contains("Mobile")) {
                event.setDeviceType("MOBILE");
            } else if (userAgent.contains("Tablet")) {
                event.setDeviceType("TABLET");
            } else {
                event.setDeviceType("DESKTOP");
            }
            
            // Extract OS info
            if (userAgent.contains("Windows")) {
                event.setOsInfo("Windows");
            } else if (userAgent.contains("Mac")) {
                event.setOsInfo("MacOS");
            } else if (userAgent.contains("Linux")) {
                event.setOsInfo("Linux");
            } else if (userAgent.contains("Android")) {
                event.setOsInfo("Android");
            } else if (userAgent.contains("iOS")) {
                event.setOsInfo("iOS");
            }
        });
    }
    
    private CompletableFuture<Void> collectLocationInformation(FraudDetectionEvent event, Map<String, String> headers) {
        String ipAddress = headers.getOrDefault("X-Forwarded-For", headers.getOrDefault("X-Real-IP", ""));
        event.setIpAddress(ipAddress);
        
        if (ipAddress.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        // Get geolocation data from IP
        WebClient webClient = webClientBuilder.build();
        return webClient.get()
            .uri(IP_GEOLOCATION_API + ipAddress)
            .retrieve()
            .bodyToMono(Map.class)
            .doOnNext(geoData -> {
                event.setGeoCountry((String) geoData.get("countryCode"));
                event.setGeoCity((String) geoData.get("city"));
                event.setGeoLatitude((Double) geoData.get("lat"));
                event.setGeoLongitude((Double) geoData.get("lon"));
                
                // Check if using proxy/VPN
                event.setVpn(Boolean.TRUE.equals(geoData.get("proxy")));
                event.setProxy(Boolean.TRUE.equals(geoData.get("hosting")));
            })
            .onErrorResume(e -> {
                logger.error("Error getting geolocation for IP: {}", ipAddress, e);
                return Mono.empty();
            })
            .toFuture()
            .thenAccept(v -> {});
    }
    
    private CompletableFuture<MLPrediction> runMLAnomalyDetection(FraudDetectionEvent event, TokenizationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Prepare features for ML model
                Map<String, Object> features = new HashMap<>();
                features.put("tokensLastHour", event.getTokensLastHour());
                features.put("tokensLastDay", event.getTokensLastDay());
                features.put("uniqueCardsLastHour", event.getUniqueCardsLastHour());
                features.put("failedAttemptsLastHour", event.getFailedAttemptsLastHour());
                features.put("isVpn", event.isVpn());
                features.put("isProxy", event.isProxy());
                features.put("deviceType", event.getDeviceType());
                features.put("hour", LocalDateTime.now().getHour());
                features.put("dayOfWeek", LocalDateTime.now().getDayOfWeek().getValue());
                
                // Simulate ML prediction (in production, this would call actual ML service)
                MLPrediction prediction = new MLPrediction();
                prediction.setModelVersion("v1.2.0");
                prediction.setAnomalyScore(calculateAnomalyScore(features));
                prediction.setFraudProbability(calculateFraudProbability(features));
                prediction.setFeatureScores(calculateFeatureScores(features));
                
                return prediction;
            } catch (Exception e) {
                logger.error("Error running ML anomaly detection", e);
                return null;
            }
        });
    }
    
    private double calculateAnomalyScore(Map<String, Object> features) {
        // Simplified anomaly score calculation
        double score = 0.0;
        
        Integer tokensLastHour = (Integer) features.get("tokensLastHour");
        if (tokensLastHour > 100) score += 0.3;
        if (tokensLastHour > 500) score += 0.4;
        
        Boolean isVpn = (Boolean) features.get("isVpn");
        if (Boolean.TRUE.equals(isVpn)) score += 0.2;
        
        Integer hour = (Integer) features.get("hour");
        if (hour < 6 || hour > 22) score += 0.1; // Unusual hours
        
        return Math.min(score, 1.0);
    }
    
    private double calculateFraudProbability(Map<String, Object> features) {
        // Simplified fraud probability calculation
        double prob = 0.0;
        
        Integer failedAttempts = (Integer) features.get("failedAttemptsLastHour");
        if (failedAttempts > 5) prob += 0.3;
        if (failedAttempts > 10) prob += 0.3;
        
        Boolean isProxy = (Boolean) features.get("isProxy");
        if (Boolean.TRUE.equals(isProxy)) prob += 0.2;
        
        Integer uniqueCards = (Integer) features.get("uniqueCardsLastHour");
        if (uniqueCards > 10) prob += 0.2;
        
        return Math.min(prob, 1.0);
    }
    
    private Map<String, Double> calculateFeatureScores(Map<String, Object> features) {
        Map<String, Double> scores = new HashMap<>();
        scores.put("velocity_score", features.get("tokensLastHour") != null ? 
            Math.min((Integer) features.get("tokensLastHour") / 100.0, 1.0) : 0.0);
        scores.put("location_score", Boolean.TRUE.equals(features.get("isVpn")) ? 0.8 : 0.2);
        scores.put("behavior_score", Math.random() * 0.5); // Simulated behavioral score
        scores.put("device_score", Math.random() * 0.3); // Simulated device reputation score
        return scores;
    }
    
    private String makeDecision(int riskScore, String riskLevel) {
        if (riskScore < 25) {
            return "ALLOW";
        } else if (riskScore < 50) {
            return "MONITOR";
        } else if (riskScore < 75) {
            return "CHALLENGE";
        } else {
            return "BLOCK";
        }
    }
    
    private void sendHighRiskAlert(FraudDetectionEvent event, TokenizationRequest request) {
        String message = String.format(
            "High risk fraud detected! Merchant: %s, Risk Score: %d, Decision: %s, Triggered Rules: %s",
            event.getMerchantId(),
            event.getRiskScore(),
            event.getDecision(),
            String.join(", ", event.getTriggeredRules())
        );
        
        notificationService.sendSecurityAlert("High Risk Fraud Alert", message, "CRITICAL");
    }
    
    private String generateDeviceFingerprint(Map<String, String> headers) {
        // Generate a simple device fingerprint based on headers
        String userAgent = headers.getOrDefault("User-Agent", "");
        String acceptLanguage = headers.getOrDefault("Accept-Language", "");
        String acceptEncoding = headers.getOrDefault("Accept-Encoding", "");
        
        return Base64.getEncoder().encodeToString(
            (userAgent + acceptLanguage + acceptEncoding).getBytes()
        ).substring(0, 16);
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two points
        final double R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    // Inner classes
    public static class FraudDetectionResult {
        private String eventId;
        private int riskScore;
        private String riskLevel;
        private String decision;
        private List<String> triggeredRules;
        private Map<String, Integer> ruleScores;
        private String deviceFingerprint;
        
        public FraudDetectionResult(String eventId, int riskScore, String riskLevel, 
                                  String decision, List<String> triggeredRules, 
                                  Map<String, Integer> ruleScores, String deviceFingerprint) {
            this.eventId = eventId;
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
            this.decision = decision;
            this.triggeredRules = triggeredRules;
            this.ruleScores = ruleScores;
            this.deviceFingerprint = deviceFingerprint;
        }
        
        // Getters and setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        
        public List<String> getTriggeredRules() { return triggeredRules; }
        public void setTriggeredRules(List<String> triggeredRules) { this.triggeredRules = triggeredRules; }
        
        public Map<String, Integer> getRuleScores() { return ruleScores; }
        public void setRuleScores(Map<String, Integer> ruleScores) { this.ruleScores = ruleScores; }
        
        public String getDeviceFingerprint() { return deviceFingerprint; }
        public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    }
    
    private static class MLPrediction {
        private String modelVersion;
        private Double anomalyScore;
        private Double fraudProbability;
        private Map<String, Double> featureScores;
        
        // Getters and setters
        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
        
        public Double getAnomalyScore() { return anomalyScore; }
        public void setAnomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; }
        
        public Double getFraudProbability() { return fraudProbability; }
        public void setFraudProbability(Double fraudProbability) { this.fraudProbability = fraudProbability; }
        
        public Map<String, Double> getFeatureScores() { return featureScores; }
        public void setFeatureScores(Map<String, Double> featureScores) { this.featureScores = featureScores; }
    }
}