package com.sabpaisa.tokenization.domain.entity;

import com.sabpaisa.tokenization.entity.Merchant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "billing_records", indexes = {
    @Index(name = "idx_merchant_billing_month", columnList = "merchant_id,billingMonth"),
    @Index(name = "idx_billing_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private LocalDate billingMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_plan_id", nullable = false)
    private PricingPlan pricingPlan;

    // Usage metrics
    private Long totalTokensCreated = 0L;
    private Long totalActiveTokens = 0L;
    private Long totalTransactions = 0L;

    // Charges breakdown (in INR)
    @Column(precision = 10, scale = 2)
    private BigDecimal tokenCreationCharges = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal storageCharges = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal transactionCharges = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal platformCharges = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingStatus status = BillingStatus.PENDING;

    private LocalDate dueDate;
    private LocalDate paidDate;

    private String invoiceNumber;
    private String paymentReference;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public enum BillingStatus {
        PENDING,
        PAID,
        OVERDUE,
        CANCELLED,
        DISPUTED
    }
}