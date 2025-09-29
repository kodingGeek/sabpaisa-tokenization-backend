package com.sabpaisa.tokenization.cloud;

import java.util.Map;
import java.util.Set;

/**
 * Interface for cloud storage connectors
 */
public interface CloudConnector {
    
    /**
     * Initialize the cloud connector
     */
    void initialize();
    
    /**
     * Store encrypted token data in the cloud
     */
    void store(EncryptedTokenData data, Map<String, String> metadata) throws CloudStorageException;
    
    /**
     * Retrieve encrypted token data from the cloud
     */
    EncryptedTokenData retrieve(String tokenId) throws CloudStorageException;
    
    /**
     * Delete token data from the cloud
     */
    void delete(String tokenId) throws CloudStorageException;
    
    /**
     * Get inventory of all token IDs
     */
    Set<String> getTokenInventory() throws CloudStorageException;
    
    /**
     * Get total count of tokens
     */
    long getTokenCount();
    
    /**
     * Check if the connector is healthy
     */
    boolean isHealthy();
    
    /**
     * Get cloud-specific metrics
     */
    Map<String, Object> getMetrics();
}