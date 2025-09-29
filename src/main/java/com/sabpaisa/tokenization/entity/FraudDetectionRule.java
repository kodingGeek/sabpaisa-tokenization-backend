package com.sabpaisa.tokenization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "fraud_detection_rules")
public class FraudDetectionRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String ruleName;
    
    @Column(nullable = false)
    private String ruleType; // VELOCITY, GEO_LOCATION, AMOUNT_THRESHOLD, ML_BASED, DEVICE_FINGERPRINT
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false)
    private int priority = 0; // Higher priority rules are evaluated first
    
    @Column(nullable = false)
    private String action; // BLOCK, FLAG, MONITOR, CHALLENGE
    
    @Column(nullable = false)
    private int riskScore; // 0-100, contributes to overall risk score
    
    @Column(columnDefinition = "TEXT")
    private String ruleExpression; // JSON expression for rule evaluation
    
    @ElementCollection
    @CollectionTable(name = "rule_parameters")
    private Map<String, String> parameters;
    
    // Analytics
    @Column(name = "total_evaluations")
    private Long totalEvaluations = 0L;
    
    @Column(name = "total_triggers")
    private Long totalTriggers = 0L;
    
    @Column(name = "false_positives")
    private Long falsePositives = 0L;
    
    @Column(name = "effectiveness_score")
    private Double effectivenessScore = 0.0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    
    public String getRuleExpression() { return ruleExpression; }
    public void setRuleExpression(String ruleExpression) { this.ruleExpression = ruleExpression; }
    
    public Map<String, String> getParameters() { return parameters; }
    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
    
    public Long getTotalEvaluations() { return totalEvaluations; }
    public void setTotalEvaluations(Long totalEvaluations) { this.totalEvaluations = totalEvaluations; }
    
    public Long getTotalTriggers() { return totalTriggers; }
    public void setTotalTriggers(Long totalTriggers) { this.totalTriggers = totalTriggers; }
    
    public Long getFalsePositives() { return falsePositives; }
    public void setFalsePositives(Long falsePositives) { this.falsePositives = falsePositives; }
    
    public Double getEffectivenessScore() { return effectivenessScore; }
    public void setEffectivenessScore(Double effectivenessScore) { this.effectivenessScore = effectivenessScore; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}