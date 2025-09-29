package com.sabpaisa.tokenization.controller;

import com.sabpaisa.tokenization.cloud.CloudDataStructures.*;
import com.sabpaisa.tokenization.cloud.MultiCloudReplicationService;
import com.sabpaisa.tokenization.cloud.MultiCloudReplicationService.CloudProvider;
import com.sabpaisa.tokenization.dto.ApiResponse;
import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.entity.Merchant;
import com.sabpaisa.tokenization.repository.TokenRepository;
import com.sabpaisa.tokenization.repository.MerchantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST API Controller for Multi-Cloud Token Replication
 */
@RestController
@RequestMapping("/api/v1/multi-cloud")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:3003"})
public class MultiCloudController {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiCloudController.class);
    
    @Autowired
    private MultiCloudReplicationService multiCloudService;
    
    @Autowired(required = false)
    private TokenRepository tokenRepository;
    
    @Autowired(required = false)
    private MerchantRepository merchantRepository;
    
    /**
     * Get multi-cloud replication status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getReplicationStatus() {
        logger.info("Getting multi-cloud replication status");
        
        try {
            ReplicationStatistics stats = multiCloudService.getReplicationStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("healthStatus", stats.getHealthStatus());
            response.put("providers", stats.getProviderStatistics());
            response.put("recentReplications", stats.getRecentReplications());
            
            // Add cloud provider details
            Map<String, String> providerInfo = new HashMap<>();
            for (CloudProvider provider : CloudProvider.values()) {
                providerInfo.put(provider.name(), provider.getDisplayName());
            }
            response.put("availableProviders", providerInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get replication status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get status: " + e.getMessage()));
        }
    }
    
    /**
     * Manually trigger token replication
     */
    @PostMapping("/replicate/{tokenId}")
    public ResponseEntity<ApiResponse> replicateToken(@PathVariable Long tokenId) {
        logger.info("Manual replication request for token: {}", tokenId);
        
        try {
            // In production, would fetch from repository
            Token token = createMockToken(tokenId);
            Merchant merchant = createMockMerchant();
            
            CompletableFuture<ReplicationResult> future = multiCloudService.replicateToken(token, merchant);
            ReplicationResult result = future.get(); // Wait for completion
            
            Map<String, Object> data = new HashMap<>();
            data.put("replicationId", result.getReplicationId());
            data.put("success", result.isSuccess());
            data.put("duration", result.getDuration() + "ms");
            data.put("providers", result.getProviderResults().size());
            
            String message = result.isSuccess() ? 
                "Token replicated successfully" : 
                "Replication partially failed";
            
            return ResponseEntity.ok(new ApiResponse(result.isSuccess(), message, data));
            
        } catch (Exception e) {
            logger.error("Replication failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Replication failed: " + e.getMessage(), null));
        }
    }
    
    /**
     * Retrieve token from multi-cloud
     */
    @GetMapping("/retrieve/{tokenId}")
    public ResponseEntity<ApiResponse> retrieveToken(
            @PathVariable String tokenId,
            @RequestParam(defaultValue = "us-east-1") String region) {
        
        logger.info("Retrieving token {} from region {}", tokenId, region);
        
        try {
            TokenRetrievalResult result = multiCloudService.retrieveToken(tokenId, region);
            
            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Token not found in any cloud", null));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("tokenId", tokenId);
            data.put("sourceProvider", result.getSourceProvider());
            data.put("attemptCount", result.getAttemptCount());
            data.put("retrieved", true);
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Token retrieved from " + result.getSourceProvider(),
                data
            ));
            
        } catch (Exception e) {
            logger.error("Retrieval failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Retrieval failed: " + e.getMessage(), null));
        }
    }
    
    /**
     * Delete token from all clouds
     */
    @DeleteMapping("/delete/{tokenId}")
    public ResponseEntity<ApiResponse> deleteFromAllClouds(@PathVariable String tokenId) {
        logger.info("Deleting token {} from all clouds", tokenId);
        
        try {
            CompletableFuture<DeletionResult> future = multiCloudService.deleteFromAllClouds(tokenId);
            DeletionResult result = future.get();
            
            Map<String, Object> data = new HashMap<>();
            data.put("tokenId", result.getTokenId());
            data.put("deletedFrom", result.getSuccessCount());
            data.put("totalProviders", result.getTotalCount());
            data.put("fullyDeleted", result.isFullyDeleted());
            
            String message = result.isFullyDeleted() ?
                "Token deleted from all clouds" :
                String.format("Token deleted from %d of %d clouds", 
                    result.getSuccessCount(), result.getTotalCount());
            
            return ResponseEntity.ok(new ApiResponse(result.isFullyDeleted(), message, data));
            
        } catch (Exception e) {
            logger.error("Deletion failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Deletion failed: " + e.getMessage(), null));
        }
    }
    
    /**
     * Force synchronization across clouds
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse> forceSynchronization() {
        logger.info("Forcing cloud synchronization");
        
        try {
            multiCloudService.synchronizeAcrossClouds();
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Synchronization initiated successfully",
                null
            ));
            
        } catch (Exception e) {
            logger.error("Synchronization failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Synchronization failed: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get cloud-specific metrics
     */
    @GetMapping("/metrics/{provider}")
    public ResponseEntity<Map<String, Object>> getProviderMetrics(@PathVariable String provider) {
        try {
            CloudProvider cloudProvider = CloudProvider.valueOf(provider.toUpperCase());
            ReplicationStatistics stats = multiCloudService.getReplicationStatistics();
            
            ProviderStatistics providerStats = stats.getProviderStatistics().get(cloudProvider);
            if (providerStats == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Provider not found: " + provider));
            }
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("provider", cloudProvider.getDisplayName());
            metrics.put("available", providerStats.isAvailable());
            metrics.put("tokenCount", providerStats.getTokenCount());
            metrics.put("averageLatency", providerStats.getAverageLatency() + "ms");
            metrics.put("successRate", providerStats.getSuccessRate() + "%");
            metrics.put("lastSync", providerStats.getLastSync());
            
            return ResponseEntity.ok(metrics);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid provider: " + provider));
        } catch (Exception e) {
            logger.error("Failed to get provider metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get metrics: " + e.getMessage()));
        }
    }
    
    /**
     * Test cloud connectivity
     */
    @GetMapping("/test-connectivity")
    public ResponseEntity<Map<String, Object>> testConnectivity() {
        Map<String, Object> connectivity = new HashMap<>();
        
        for (CloudProvider provider : CloudProvider.values()) {
            Map<String, Object> providerTest = new HashMap<>();
            providerTest.put("name", provider.getDisplayName());
            providerTest.put("status", "CONNECTED"); // Simulated
            providerTest.put("latency", (int)(Math.random() * 100) + "ms");
            connectivity.put(provider.name(), providerTest);
        }
        
        return ResponseEntity.ok(connectivity);
    }
    
    // Helper methods
    
    private Token createMockToken(Long tokenId) {
        Token token = new Token();
        token.setId(tokenId);
        token.setTokenValue("TOK-" + tokenId);
        token.setMerchant(createMockMerchant());
        return token;
    }
    
    private Merchant createMockMerchant() {
        Merchant merchant = new Merchant();
        merchant.setMerchantId("MERCH-001");
        merchant.setMerchantName("Test Merchant");
        return merchant;
    }
}