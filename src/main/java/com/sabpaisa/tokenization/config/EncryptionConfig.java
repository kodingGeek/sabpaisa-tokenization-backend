package com.sabpaisa.tokenization.config;

import com.sabpaisa.tokenization.service.AesGcmEncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class EncryptionConfig {
    
    @Value("${app.encryption.master-key:}")
    private String masterKey;
    
    @Bean
    public CommandLineRunner encryptionKeyValidator(AesGcmEncryptionService encryptionService) {
        return args -> {
            if (masterKey == null || masterKey.isEmpty()) {
                System.err.println("===========================================");
                System.err.println("WARNING: No encryption master key configured!");
                System.err.println("Generate a new key with:");
                System.err.println("  " + AesGcmEncryptionService.generateNewKey());
                System.err.println("Then set it in application.properties:");
                System.err.println("  app.encryption.master-key=<generated-key>");
                System.err.println("===========================================");
            }
        };
    }
    
    @Bean(name = "bulkProcessingExecutor")
    public Executor bulkProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("BulkProcess-");
        executor.initialize();
        return executor;
    }
}