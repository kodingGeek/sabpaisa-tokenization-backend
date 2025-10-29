package com.sabpaisa.tokenization.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple health controller that responds immediately for ELB health checks
 * This bypasses complex Spring Boot Actuator health checks during startup
 */
@RestController
@ConditionalOnWebApplication
@Order(1) // Load early
public class SimpleHealthController {
    
    /**
     * Simple health endpoint for ELB/ALB health checks
     * Returns immediately without waiting for full application startup
     */
    @GetMapping("/api/actuator/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "tokenization-backend");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Root health endpoint - alternative path
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "tokenization-backend");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Immediate health check - no dependencies
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}