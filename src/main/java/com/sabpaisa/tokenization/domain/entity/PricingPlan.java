package com.sabpaisa.tokenization.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pricing_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String planCode;

    @Column(nullable = false)
    private String planName;

    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Pricing components (in INR)
    @Column(precision = 10, scale = 2)
    private BigDecimal perTokenCreationPrice = new BigDecimal("0.10");

    @Column(precision = 10, scale = 2)
    private BigDecimal perTokenStoragePrice = new BigDecimal("0.05"); // Per month

    @Column(precision = 10, scale = 2)
    private BigDecimal perTransactionPrice = new BigDecimal("0.02");

    @Column(precision = 10, scale = 2)
    private BigDecimal perPlatformPrice = new BigDecimal("500.00"); // Per month

    // Plan limits
    private Integer freeTokensPerMonth = 1000;
    private Integer maxTokensPerMonth = 1000000;
    private Integer maxPlatforms = 10;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}