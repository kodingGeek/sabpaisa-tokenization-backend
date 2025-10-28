package com.sabpaisa.tokenization.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom health checks that work with ECS/ALB
 */
@Configuration
public class HealthCheckConfig {
    
    /**
     * Custom health indicator that reports UP immediately after startup
     * This prevents ECS from killing the container during the long startup process
     */
    @Bean
    public HealthIndicator applicationHealthIndicator() {
        return () -> Health.up()
                .withDetail("application", "tokenization-backend")
                .withDetail("status", "ready")
                .build();
    }
}