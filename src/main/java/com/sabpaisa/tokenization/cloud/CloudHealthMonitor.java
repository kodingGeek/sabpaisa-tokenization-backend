package com.sabpaisa.tokenization.cloud;

import com.sabpaisa.tokenization.cloud.MultiCloudReplicationService.CloudProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Monitors health of cloud providers
 */
@Component
public class CloudHealthMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(CloudHealthMonitor.class);
    
    private final Map<CloudProvider, HealthMetrics> healthMetrics = new ConcurrentHashMap<>();
    private Map<CloudProvider, CloudConnector> connectors;
    
    public void startMonitoring(Map<CloudProvider, CloudConnector> cloudConnectors) {
        this.connectors = cloudConnectors;
        
        // Initialize health metrics
        for (CloudProvider provider : CloudProvider.values()) {
            healthMetrics.put(provider, new HealthMetrics());
        }
    }
    
    /**
     * Check health of all providers
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void performHealthCheck() {
        if (connectors == null) return;
        
        for (Map.Entry<CloudProvider, CloudConnector> entry : connectors.entrySet()) {
            CloudProvider provider = entry.getKey();
            CloudConnector connector = entry.getValue();
            
            try {
                boolean healthy = connector.isHealthy();
                HealthMetrics metrics = healthMetrics.get(provider);
                
                if (healthy) {
                    metrics.lastSuccessfulCheck = LocalDateTime.now();
                    metrics.consecutiveFailures = 0;
                    metrics.available = true;
                } else {
                    metrics.consecutiveFailures++;
                    if (metrics.consecutiveFailures >= 3) {
                        metrics.available = false;
                        logger.warn("Provider {} marked as unavailable after {} failures", 
                                   provider, metrics.consecutiveFailures);
                    }
                }
                
            } catch (Exception e) {
                logger.error("Health check failed for {}", provider, e);
                HealthMetrics metrics = healthMetrics.get(provider);
                metrics.consecutiveFailures++;
                if (metrics.consecutiveFailures >= 3) {
                    metrics.available = false;
                }
            }
        }
    }
    
    public void recordSuccess(CloudProvider provider, long latency) {
        HealthMetrics metrics = healthMetrics.get(provider);
        if (metrics != null) {
            metrics.successCount.incrementAndGet();
            metrics.totalLatency.add(latency);
            metrics.operationCount.incrementAndGet();
            metrics.lastSuccessTime = LocalDateTime.now();
        }
    }
    
    public void recordFailure(CloudProvider provider, long latency, String error) {
        HealthMetrics metrics = healthMetrics.get(provider);
        if (metrics != null) {
            metrics.failureCount.incrementAndGet();
            metrics.totalLatency.add(latency);
            metrics.operationCount.incrementAndGet();
            metrics.lastError = error;
            metrics.lastFailureTime = LocalDateTime.now();
        }
    }
    
    public boolean isProviderHealthy(CloudProvider provider) {
        HealthMetrics metrics = healthMetrics.get(provider);
        return metrics != null && metrics.available;
    }
    
    public double getAverageLatency(CloudProvider provider) {
        HealthMetrics metrics = healthMetrics.get(provider);
        if (metrics == null || metrics.operationCount.get() == 0) {
            return 0.0;
        }
        return metrics.totalLatency.doubleValue() / metrics.operationCount.get();
    }
    
    public double getSuccessRate(CloudProvider provider) {
        HealthMetrics metrics = healthMetrics.get(provider);
        if (metrics == null || metrics.operationCount.get() == 0) {
            return 0.0;
        }
        return (double) metrics.successCount.get() / metrics.operationCount.get() * 100;
    }
    
    public LocalDateTime getLastSuccessfulSync(CloudProvider provider) {
        HealthMetrics metrics = healthMetrics.get(provider);
        return metrics != null ? metrics.lastSuccessTime : null;
    }
    
    private static class HealthMetrics {
        volatile boolean available = true;
        AtomicLong successCount = new AtomicLong();
        AtomicLong failureCount = new AtomicLong();
        AtomicLong operationCount = new AtomicLong();
        DoubleAdder totalLatency = new DoubleAdder();
        LocalDateTime lastSuccessTime;
        LocalDateTime lastFailureTime;
        LocalDateTime lastSuccessfulCheck;
        String lastError;
        int consecutiveFailures = 0;
    }
}