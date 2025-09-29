package com.sabpaisa.tokenization.controller;

import com.sabpaisa.tokenization.dto.ApiResponse;
import com.sabpaisa.tokenization.entity.FraudDetectionEvent;
import com.sabpaisa.tokenization.entity.FraudDetectionRule;
import com.sabpaisa.tokenization.service.FraudDetectionService;
import com.sabpaisa.tokenization.repository.FraudDetectionEventRepository;
import com.sabpaisa.tokenization.repository.FraudDetectionRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/fraud-detection")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:3003"})
public class FraudDetectionController {
    
    @Autowired
    private FraudDetectionService fraudDetectionService;
    
    @Autowired
    private FraudDetectionEventRepository eventRepository;
    
    @Autowired
    private FraudDetectionRuleRepository ruleRepository;
    
    // Dashboard endpoints
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(defaultValue = "24") int hoursBack) {
        
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventRepository.count());
        stats.put("highRiskEvents", eventRepository.countHighRiskEvents(since));
        stats.put("averageRiskScore", eventRepository.getAverageRiskScore(since));
        stats.put("activeRules", ruleRepository.countActiveRules());
        stats.put("topRiskCountries", eventRepository.getTopRiskCountries(since));
        stats.put("hourlyDistribution", eventRepository.getHighRiskHourlyDistribution(since));
        
        return ResponseEntity.ok(stats);
    }
    
    // Event management endpoints
    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<FraudDetectionEvent> events;
        
        if (startDate != null && endDate != null) {
            events = eventRepository.findByDateRange(startDate, endDate);
        } else if (merchantId != null) {
            events = eventRepository.findByMerchantId(merchantId);
        } else if (riskLevel != null) {
            events = eventRepository.findByRiskLevel(riskLevel);
        } else if (decision != null) {
            events = eventRepository.findByDecision(decision);
        } else {
            events = eventRepository.findAll();
        }
        
        // Pagination
        int start = page * size;
        int end = Math.min(start + size, events.size());
        List<FraudDetectionEvent> paginatedEvents = events.subList(start, end);
        
        Map<String, Object> response = new HashMap<>();
        response.put("events", paginatedEvents);
        response.put("totalElements", events.size());
        response.put("totalPages", (int) Math.ceil((double) events.size() / size));
        response.put("currentPage", page);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/events/{eventId}")
    public ResponseEntity<FraudDetectionEvent> getEvent(@PathVariable String eventId) {
        Optional<FraudDetectionEvent> event = eventRepository.findAll().stream()
            .filter(e -> e.getEventId().equals(eventId))
            .findFirst();
        
        return event.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/events/{eventId}/review")
    public ResponseEntity<ApiResponse> reviewEvent(
            @PathVariable String eventId,
            @RequestBody Map<String, Object> reviewData) {
        
        Optional<FraudDetectionEvent> eventOpt = eventRepository.findAll().stream()
            .filter(e -> e.getEventId().equals(eventId))
            .findFirst();
        
        if (eventOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        FraudDetectionEvent event = eventOpt.get();
        event.setManualReviewNotes((String) reviewData.get("notes"));
        event.setReviewedBy((String) reviewData.get("reviewedBy"));
        event.setReviewedAt(LocalDateTime.now());
        event.setIsFalsePositive((Boolean) reviewData.getOrDefault("isFalsePositive", false));
        
        eventRepository.save(event);
        
        // Update rule effectiveness if marked as false positive
        if (Boolean.TRUE.equals(event.getIsFalsePositive())) {
            updateRuleEffectiveness(event.getTriggeredRules());
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Event reviewed successfully", null));
    }
    
    // Rule management endpoints
    @GetMapping("/rules")
    public ResponseEntity<List<FraudDetectionRule>> getRules(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String ruleType) {
        
        List<FraudDetectionRule> rules;
        
        if (active != null && active) {
            rules = ruleRepository.findByActiveTrue();
        } else if (ruleType != null) {
            rules = ruleRepository.findByRuleType(ruleType);
        } else {
            rules = ruleRepository.findAll();
        }
        
        return ResponseEntity.ok(rules);
    }
    
    @GetMapping("/rules/{ruleId}")
    public ResponseEntity<FraudDetectionRule> getRule(@PathVariable Long ruleId) {
        return ruleRepository.findById(ruleId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/rules")
    public ResponseEntity<FraudDetectionRule> createRule(@RequestBody FraudDetectionRule rule) {
        rule.setCreatedBy("SYSTEM"); // In production, get from security context
        FraudDetectionRule savedRule = ruleRepository.save(rule);
        return ResponseEntity.ok(savedRule);
    }
    
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<FraudDetectionRule> updateRule(
            @PathVariable Long ruleId,
            @RequestBody FraudDetectionRule updatedRule) {
        
        return ruleRepository.findById(ruleId)
            .map(rule -> {
                rule.setRuleName(updatedRule.getRuleName());
                rule.setDescription(updatedRule.getDescription());
                rule.setRuleType(updatedRule.getRuleType());
                rule.setActive(updatedRule.isActive());
                rule.setPriority(updatedRule.getPriority());
                rule.setAction(updatedRule.getAction());
                rule.setRiskScore(updatedRule.getRiskScore());
                rule.setRuleExpression(updatedRule.getRuleExpression());
                rule.setParameters(updatedRule.getParameters());
                
                return ResponseEntity.ok(ruleRepository.save(rule));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<ApiResponse> deleteRule(@PathVariable Long ruleId) {
        if (ruleRepository.existsById(ruleId)) {
            ruleRepository.deleteById(ruleId);
            return ResponseEntity.ok(new ApiResponse(true, "Rule deleted successfully", null));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/rules/{ruleId}/toggle")
    public ResponseEntity<FraudDetectionRule> toggleRule(@PathVariable Long ruleId) {
        return ruleRepository.findById(ruleId)
            .map(rule -> {
                rule.setActive(!rule.isActive());
                return ResponseEntity.ok(ruleRepository.save(rule));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Analytics endpoints
    @GetMapping("/analytics/rule-effectiveness")
    public ResponseEntity<List<Map<String, Object>>> getRuleEffectiveness() {
        List<FraudDetectionRule> rules = ruleRepository.findAll();
        
        List<Map<String, Object>> effectiveness = rules.stream()
            .map(rule -> {
                Map<String, Object> data = new HashMap<>();
                data.put("ruleName", rule.getRuleName());
                data.put("totalEvaluations", rule.getTotalEvaluations());
                data.put("totalTriggers", rule.getTotalTriggers());
                data.put("triggerRate", rule.getTotalEvaluations() > 0 ? 
                    (double) rule.getTotalTriggers() / rule.getTotalEvaluations() : 0.0);
                data.put("effectivenessScore", rule.getEffectivenessScore());
                data.put("falsePositives", rule.getFalsePositives());
                return data;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(effectiveness);
    }
    
    @GetMapping("/analytics/risk-trends")
    public ResponseEntity<Map<String, Object>> getRiskTrends(
            @RequestParam(defaultValue = "7") int days) {
        
        Map<String, Object> trends = new HashMap<>();
        List<Map<String, Object>> dailyTrends = new ArrayList<>();
        
        for (int i = 0; i < days; i++) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            List<FraudDetectionEvent> dayEvents = eventRepository.findByDateRange(dayStart, dayEnd);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayStart.toLocalDate());
            dayData.put("totalEvents", dayEvents.size());
            dayData.put("highRiskEvents", dayEvents.stream()
                .filter(e -> "HIGH".equals(e.getRiskLevel()) || "CRITICAL".equals(e.getRiskLevel()))
                .count());
            dayData.put("blockedEvents", dayEvents.stream()
                .filter(e -> "BLOCK".equals(e.getDecision()))
                .count());
            dayData.put("averageRiskScore", dayEvents.stream()
                .mapToInt(FraudDetectionEvent::getRiskScore)
                .average()
                .orElse(0.0));
            
            dailyTrends.add(dayData);
        }
        
        trends.put("dailyTrends", dailyTrends);
        trends.put("period", days + " days");
        
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/analytics/geo-analysis")
    public ResponseEntity<List<Map<String, Object>>> getGeoAnalysis() {
        List<Object[]> countryData = eventRepository.getTopRiskCountries(LocalDateTime.now().minusDays(30));
        
        List<Map<String, Object>> geoAnalysis = countryData.stream()
            .map(data -> {
                Map<String, Object> country = new HashMap<>();
                country.put("country", data[0]);
                country.put("blockedCount", data[1]);
                return country;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(geoAnalysis);
    }
    
    // Real-time monitoring endpoint (WebSocket would be better in production)
    @GetMapping("/monitor/live")
    public ResponseEntity<List<FraudDetectionEvent>> getLiveEvents(
            @RequestParam(defaultValue = "5") int minutes) {
        
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        List<FraudDetectionEvent> recentEvents = eventRepository.findAll().stream()
            .filter(e -> e.getCreatedAt().isAfter(since))
            .sorted(Comparator.comparing(FraudDetectionEvent::getCreatedAt).reversed())
            .limit(100)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(recentEvents);
    }
    
    private void updateRuleEffectiveness(List<String> triggeredRules) {
        for (String ruleName : triggeredRules) {
            ruleRepository.findByRuleName(ruleName).ifPresent(rule -> {
                rule.setFalsePositives(rule.getFalsePositives() + 1);
                // Recalculate effectiveness score
                double effectiveness = 1.0 - ((double) rule.getFalsePositives() / rule.getTotalTriggers());
                rule.setEffectivenessScore(Math.max(0, effectiveness));
                ruleRepository.save(rule);
            });
        }
    }
    
    // Initialize default rules
    @PostMapping("/rules/initialize-defaults")
    public ResponseEntity<ApiResponse> initializeDefaultRules() {
        List<FraudDetectionRule> defaultRules = createDefaultRules();
        ruleRepository.saveAll(defaultRules);
        return ResponseEntity.ok(new ApiResponse(true, 
            "Initialized " + defaultRules.size() + " default fraud detection rules", null));
    }
    
    private List<FraudDetectionRule> createDefaultRules() {
        List<FraudDetectionRule> rules = new ArrayList<>();
        
        // Velocity rule
        FraudDetectionRule velocityRule = new FraudDetectionRule();
        velocityRule.setRuleName("High Velocity Tokenization");
        velocityRule.setRuleType("VELOCITY");
        velocityRule.setDescription("Triggers when too many tokens are created in a short time");
        velocityRule.setPriority(90);
        velocityRule.setAction("CHALLENGE");
        velocityRule.setRiskScore(40);
        Map<String, String> velocityParams = new HashMap<>();
        velocityParams.put("maxTokensPerHour", "50");
        velocityParams.put("maxUniqueCardsPerHour", "10");
        velocityRule.setParameters(velocityParams);
        rules.add(velocityRule);
        
        // Geo-location rule
        FraudDetectionRule geoRule = new FraudDetectionRule();
        geoRule.setRuleName("Suspicious Location");
        geoRule.setRuleType("GEO_LOCATION");
        geoRule.setDescription("Triggers for VPN/Proxy usage or blocked countries");
        geoRule.setPriority(85);
        geoRule.setAction("BLOCK");
        geoRule.setRiskScore(60);
        Map<String, String> geoParams = new HashMap<>();
        geoParams.put("blockVPN", "true");
        geoParams.put("blockProxy", "true");
        geoParams.put("blockTor", "true");
        geoParams.put("blockedCountries", "XX,YY"); // Example country codes
        geoParams.put("checkImpossibleTravel", "true");
        geoRule.setParameters(geoParams);
        rules.add(geoRule);
        
        // ML-based rule
        FraudDetectionRule mlRule = new FraudDetectionRule();
        mlRule.setRuleName("ML Anomaly Detection");
        mlRule.setRuleType("ML_BASED");
        mlRule.setDescription("Triggers when ML model detects high fraud probability");
        mlRule.setPriority(95);
        mlRule.setAction("MONITOR");
        mlRule.setRiskScore(50);
        Map<String, String> mlParams = new HashMap<>();
        mlParams.put("fraudProbabilityThreshold", "0.7");
        mlParams.put("anomalyScoreThreshold", "0.8");
        mlRule.setParameters(mlParams);
        rules.add(mlRule);
        
        // Device fingerprint rule
        FraudDetectionRule deviceRule = new FraudDetectionRule();
        deviceRule.setRuleName("Blacklisted Device");
        deviceRule.setRuleType("DEVICE_FINGERPRINT");
        deviceRule.setDescription("Triggers for devices previously involved in fraud");
        deviceRule.setPriority(100);
        deviceRule.setAction("BLOCK");
        deviceRule.setRiskScore(80);
        Map<String, String> deviceParams = new HashMap<>();
        deviceParams.put("checkBlacklistedDevices", "true");
        deviceRule.setParameters(deviceParams);
        rules.add(deviceRule);
        
        return rules;
    }
}