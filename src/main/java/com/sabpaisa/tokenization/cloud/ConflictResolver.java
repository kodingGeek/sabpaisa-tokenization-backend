package com.sabpaisa.tokenization.cloud;

import com.sabpaisa.tokenization.cloud.CloudDataStructures.EncryptedTokenData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Resolves conflicts between different versions of tokens across clouds
 */
@Component
public class ConflictResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(ConflictResolver.class);
    
    /**
     * Resolve conflicts between multiple versions of the same token
     */
    public EncryptedTokenData resolveConflict(List<EncryptedTokenData> versions) {
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        
        if (versions.size() == 1) {
            return versions.get(0);
        }
        
        logger.info("Resolving conflict between {} versions of token {}", 
                   versions.size(), versions.get(0).getTokenId());
        
        // Strategy 1: Most recent timestamp wins
        EncryptedTokenData mostRecent = versions.stream()
            .max(Comparator.comparing(EncryptedTokenData::getTimestamp))
            .orElse(versions.get(0));
        
        // Log conflict resolution
        logger.info("Conflict resolved. Selected version with timestamp: {}", 
                   mostRecent.getTimestamp());
        
        return mostRecent;
    }
    
    /**
     * Check if two versions are in conflict
     */
    public boolean hasConflict(EncryptedTokenData version1, EncryptedTokenData version2) {
        if (version1 == null || version2 == null) {
            return false;
        }
        
        // Check if token IDs match
        if (!version1.getTokenId().equals(version2.getTokenId())) {
            return false;
        }
        
        // Check if timestamps are different
        return !version1.getTimestamp().equals(version2.getTimestamp());
    }
    
    /**
     * Merge metadata from multiple versions
     */
    public void mergeMetadata(EncryptedTokenData target, List<EncryptedTokenData> sources) {
        if (target.getMetadata() == null) {
            target.setMetadata(new java.util.HashMap<>());
        }
        
        for (EncryptedTokenData source : sources) {
            if (source.getMetadata() != null) {
                // Merge metadata, preferring newer values
                source.getMetadata().forEach((key, value) -> {
                    if (!target.getMetadata().containsKey(key) || 
                        source.getTimestamp().isAfter(target.getTimestamp())) {
                        target.getMetadata().put(key, value);
                    }
                });
            }
        }
        
        // Add conflict resolution metadata
        target.getMetadata().put("conflictResolved", "true");
        target.getMetadata().put("conflictResolvedAt", LocalDateTime.now().toString());
        target.getMetadata().put("conflictVersionCount", String.valueOf(sources.size()));
    }
}