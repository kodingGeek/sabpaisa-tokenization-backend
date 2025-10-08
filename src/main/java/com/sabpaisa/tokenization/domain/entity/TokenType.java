package com.sabpaisa.tokenization.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String typeCode;

    @Column(nullable = false)
    private String typeName;

    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Token lifecycle in days
    private Integer defaultExpiryDays = 365;

    // Maximum tokens allowed per card for this type
    private Integer maxTokensPerCard = 10;

    // Whether this token type supports multiple platforms
    private Boolean supportMultiplePlatforms = true;

    // Configuration for token generation
    @Column(columnDefinition = "JSON")
    private String tokenConfig;
}