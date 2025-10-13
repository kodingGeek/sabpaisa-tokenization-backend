package com.sabpaisa.tokenization.domain.entity;

import com.sabpaisa.tokenization.entity.Merchant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", indexes = {
    @Index(name = "idx_token_value", columnList = "tokenValue"),
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_platform_id", columnList = "platform_id"),
    @Index(name = "idx_expiry_date", columnList = "expiryDate"),
    @Index(name = "idx_card_hash_platform", columnList = "cardHash,platform_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenValue;

    @Column(nullable = false)
    private String maskedPan;

    @Column(nullable = false)
    private String cardHash; // Hash of card number for grouping tokens

    @Column(nullable = false)
    private String cardBin;

    @Column(nullable = false)
    private String cardLast4;

    private String cardType;
    private String cardBrand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_type_id", nullable = false)
    private TokenType tokenType;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant lastUsedAt;
    private Long usageCount = 0L;

    // Customer information
    private String customerEmail;
    private String customerPhone;
    private String customerId;

    // Notification settings
    private Boolean notificationEnabled = true;
    private Integer daysBeforeExpiryNotification = 30;
    private LocalDateTime lastNotificationSent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status = TokenStatus.ACTIVE;

    // Monetization tracking
    private Boolean isChargeable = true;
    private Instant chargedAt;
    
    public enum TokenStatus {
        ACTIVE,
        EXPIRED,
        SUSPENDED,
        REVOKED,
        PENDING_RENEWAL
    }
}