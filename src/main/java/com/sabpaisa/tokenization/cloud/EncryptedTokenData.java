package com.sabpaisa.tokenization.cloud;

import java.time.LocalDateTime;
import java.util.Map;

public class EncryptedTokenData {
    private String tokenId;
    private String encryptedData;
    private String merchantId;
    private LocalDateTime createdAt;
    private String checksum;
    private byte[] iv;
    private String algorithm;
    private LocalDateTime timestamp;
    private Map<String, String> metadata;
    
    public EncryptedTokenData() {}
    
    public EncryptedTokenData(String tokenId, String encryptedData, String merchantId, LocalDateTime createdAt, String checksum) {
        this.tokenId = tokenId;
        this.encryptedData = encryptedData;
        this.merchantId = merchantId;
        this.createdAt = createdAt;
        this.checksum = checksum;
    }
    
    // Getters and Setters
    public String getTokenId() {
        return tokenId;
    }
    
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
    
    public String getEncryptedData() {
        return encryptedData;
    }
    
    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }
    
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    public byte[] getIv() {
        return iv;
    }
    
    public void setIv(byte[] iv) {
        this.iv = iv;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}