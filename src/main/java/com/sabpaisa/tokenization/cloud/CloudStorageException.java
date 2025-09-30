package com.sabpaisa.tokenization.cloud;

/**
 * Exception for cloud storage operations
 */
public class CloudStorageException extends Exception {
    
    private final String provider;
    private final String operation;
    
    public CloudStorageException(String provider, String operation, String message) {
        super(String.format("[%s] %s failed: %s", provider, operation, message));
        this.provider = provider;
        this.operation = operation;
    }
    
    public CloudStorageException(String provider, String operation, String message, Throwable cause) {
        super(String.format("[%s] %s failed: %s", provider, operation, message), cause);
        this.provider = provider;
        this.operation = operation;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public String getOperation() {
        return operation;
    }
}