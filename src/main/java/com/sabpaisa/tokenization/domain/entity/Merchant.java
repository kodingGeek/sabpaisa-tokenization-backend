package com.sabpaisa.tokenization.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String businessName;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "business_type")
    private String businessType;
    
    @Column(name = "business_address", length = 500)
    private String businessAddress;
    
    @Column(name = "pan_number")
    private String panNumber;
    
    @Column(name = "gst_number")
    private String gstNumber;
    
    @Column(nullable = false)
    private String status; // ACTIVE, SUSPENDED, INACTIVE
    
    @Column(name = "webhook_url")
    private String webhookUrl;
    
    @Column(nullable = false)
    private String apiKey;
    
    @Column(nullable = false)
    private String apiSecret;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    private Set<com.sabpaisa.tokenization.entity.Token> tokens = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_plan_id")
    private PricingPlan pricingPlan;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructor
    public Merchant(String merchantId, String businessName, String email) {
        this.merchantId = merchantId;
        this.businessName = businessName;
        this.email = email;
        this.status = "ACTIVE";
    }
}