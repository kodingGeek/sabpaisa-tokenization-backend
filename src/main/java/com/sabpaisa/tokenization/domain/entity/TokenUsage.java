package com.sabpaisa.tokenization.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_usage", indexes = {
    @Index(name = "idx_merchant_usage_time", columnList = "merchant_id,usageTime"),
    @Index(name = "idx_token_usage", columnList = "token_id"),
    @Index(name = "idx_platform_usage", columnList = "platform_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id", nullable = false)
    private EnhancedToken token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Column(nullable = false)
    private String transactionType;

    @Column(nullable = false)
    private LocalDateTime usageTime;

    private String transactionReference;
    private String ipAddress;
    private String userAgent;
}