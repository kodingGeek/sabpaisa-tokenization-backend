package com.sabpaisa.tokenization.cloud;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data structures for cloud replication
 */
public class CloudDataStructures {
    
    public static class EncryptedTokenData {
        private String tokenId;
        private byte[] encryptedData;
        private byte[] iv;
        private String algorithm;
        private LocalDateTime timestamp;
        private Map<String, String> metadata;
        
        // Getters and setters
        public String getTokenId() { return tokenId; }
        public void setTokenId(String tokenId) { this.tokenId = tokenId; }
        
        public byte[] getEncryptedData() { return encryptedData; }
        public void setEncryptedData(byte[] encryptedData) { this.encryptedData = encryptedData; }
        
        public byte[] getIv() { return iv; }
        public void setIv(byte[] iv) { this.iv = iv; }
        
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }
    
    public static class ReplicationStatus {
        private String replicationId;
        private Long tokenId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private long duration;
        private int successfulProviders;
        private int totalProviders;
        private Map<String, String> details;
        
        // Getters and setters
        public String getReplicationId() { return replicationId; }
        public void setReplicationId(String replicationId) { this.replicationId = replicationId; }
        
        public Long getTokenId() { return tokenId; }
        public void setTokenId(Long tokenId) { this.tokenId = tokenId; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public int getSuccessfulProviders() { return successfulProviders; }
        public void setSuccessfulProviders(int successfulProviders) { this.successfulProviders = successfulProviders; }
        
        public int getTotalProviders() { return totalProviders; }
        public void setTotalProviders(int totalProviders) { this.totalProviders = totalProviders; }
        
        public Map<String, String> getDetails() { return details; }
        public void setDetails(Map<String, String> details) { this.details = details; }
    }
    
    public static class CloudReplicationResult {
        private MultiCloudReplicationService.CloudProvider provider;
        private boolean success;
        private long duration;
        private String error;
        private LocalDateTime timestamp;
        
        // Getters and setters
        public MultiCloudReplicationService.CloudProvider getProvider() { return provider; }
        public void setProvider(MultiCloudReplicationService.CloudProvider provider) { this.provider = provider; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class ReplicationResult {
        private String replicationId;
        private boolean success;
        private List<CloudReplicationResult> providerResults;
        private long duration;
        
        // Getters and setters
        public String getReplicationId() { return replicationId; }
        public void setReplicationId(String replicationId) { this.replicationId = replicationId; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public List<CloudReplicationResult> getProviderResults() { return providerResults; }
        public void setProviderResults(List<CloudReplicationResult> providerResults) { this.providerResults = providerResults; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
    }
    
    public static class TokenRetrievalResult {
        private boolean success;
        private com.sabpaisa.tokenization.entity.Token token;
        private MultiCloudReplicationService.CloudProvider sourceProvider;
        private int attemptCount;
        
        public TokenRetrievalResult(boolean success, com.sabpaisa.tokenization.entity.Token token,
                                  MultiCloudReplicationService.CloudProvider sourceProvider, int attemptCount) {
            this.success = success;
            this.token = token;
            this.sourceProvider = sourceProvider;
            this.attemptCount = attemptCount;
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public com.sabpaisa.tokenization.entity.Token getToken() { return token; }
        public void setToken(com.sabpaisa.tokenization.entity.Token token) { this.token = token; }
        
        public MultiCloudReplicationService.CloudProvider getSourceProvider() { return sourceProvider; }
        public void setSourceProvider(MultiCloudReplicationService.CloudProvider sourceProvider) { this.sourceProvider = sourceProvider; }
        
        public int getAttemptCount() { return attemptCount; }
        public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    }
    
    public static class DeletionResult {
        private String tokenId;
        private long successCount;
        private long totalCount;
        
        public DeletionResult(String tokenId, long successCount, long totalCount) {
            this.tokenId = tokenId;
            this.successCount = successCount;
            this.totalCount = totalCount;
        }
        
        public boolean isFullyDeleted() {
            return successCount == totalCount;
        }
        
        // Getters and setters
        public String getTokenId() { return tokenId; }
        public void setTokenId(String tokenId) { this.tokenId = tokenId; }
        
        public long getSuccessCount() { return successCount; }
        public void setSuccessCount(long successCount) { this.successCount = successCount; }
        
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    }
    
    public static class ReplicationStatistics {
        private String healthStatus;
        private Map<MultiCloudReplicationService.CloudProvider, ProviderStatistics> providerStatistics;
        private List<ReplicationStatus> recentReplications;
        private long totalReplications;
        private double overallSuccessRate;
        
        // Getters and setters
        public String getHealthStatus() { return healthStatus; }
        public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
        
        public Map<MultiCloudReplicationService.CloudProvider, ProviderStatistics> getProviderStatistics() { return providerStatistics; }
        public void setProviderStatistics(Map<MultiCloudReplicationService.CloudProvider, ProviderStatistics> providerStatistics) { this.providerStatistics = providerStatistics; }
        
        public List<ReplicationStatus> getRecentReplications() { return recentReplications; }
        public void setRecentReplications(List<ReplicationStatus> recentReplications) { this.recentReplications = recentReplications; }
        
        public long getTotalReplications() { return totalReplications; }
        public void setTotalReplications(long totalReplications) { this.totalReplications = totalReplications; }
        
        public double getOverallSuccessRate() { return overallSuccessRate; }
        public void setOverallSuccessRate(double overallSuccessRate) { this.overallSuccessRate = overallSuccessRate; }
    }
    
    public static class ProviderStatistics {
        private boolean available;
        private long tokenCount;
        private double averageLatency;
        private double successRate;
        private LocalDateTime lastSync;
        private Map<String, Object> customMetrics;
        
        // Getters and setters
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public long getTokenCount() { return tokenCount; }
        public void setTokenCount(long tokenCount) { this.tokenCount = tokenCount; }
        
        public double getAverageLatency() { return averageLatency; }
        public void setAverageLatency(double averageLatency) { this.averageLatency = averageLatency; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public LocalDateTime getLastSync() { return lastSync; }
        public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }
        
        public Map<String, Object> getCustomMetrics() { return customMetrics; }
        public void setCustomMetrics(Map<String, Object> customMetrics) { this.customMetrics = customMetrics; }
    }
}