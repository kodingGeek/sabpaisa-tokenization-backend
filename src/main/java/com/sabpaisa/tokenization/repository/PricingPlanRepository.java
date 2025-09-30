package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.domain.entity.PricingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long> {
    
    Optional<PricingPlan> findByPlanCode(String planCode);
    
    Optional<PricingPlan> findByPlanCodeAndIsActiveTrue(String planCode);
}