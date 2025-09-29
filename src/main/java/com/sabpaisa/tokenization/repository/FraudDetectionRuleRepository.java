package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.FraudDetectionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudDetectionRuleRepository extends JpaRepository<FraudDetectionRule, Long> {
    
    List<FraudDetectionRule> findByActiveTrue();
    
    List<FraudDetectionRule> findByRuleType(String ruleType);
    
    Optional<FraudDetectionRule> findByRuleName(String ruleName);
    
    List<FraudDetectionRule> findByActiveTrueOrderByPriorityDesc();
    
    @Query("SELECT r FROM FraudDetectionRule r WHERE r.active = true AND r.ruleType IN :ruleTypes ORDER BY r.priority DESC")
    List<FraudDetectionRule> findActiveRulesByTypes(@Param("ruleTypes") List<String> ruleTypes);
    
    @Query("SELECT r FROM FraudDetectionRule r WHERE r.effectivenessScore < :threshold AND r.active = true")
    List<FraudDetectionRule> findIneffectiveRules(@Param("threshold") Double threshold);
    
    @Query("SELECT COUNT(r) FROM FraudDetectionRule r WHERE r.active = true")
    long countActiveRules();
    
    @Query("SELECT AVG(r.effectivenessScore) FROM FraudDetectionRule r WHERE r.active = true")
    Double getAverageEffectivenessScore();
}