package com.sabpaisa.tokenization.config;

import com.sabpaisa.tokenization.domain.entity.Merchant;
import com.sabpaisa.tokenization.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final MerchantRepository merchantRepository;
    
    @Autowired
    public DataInitializer(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }
    
    @Override
    public void run(String... args) {
        // Create test merchant if not exists
        String testMerchantId = "MERCH001";
        
        if (!merchantRepository.existsByMerchantId(testMerchantId)) {
            Merchant testMerchant = new Merchant(testMerchantId, "Test Merchant", "test@sabpaisa.com");
            testMerchant.setApiKey(UUID.randomUUID().toString());
            testMerchant.setApiSecret(UUID.randomUUID().toString());
            
            merchantRepository.save(testMerchant);
            
            System.out.println("===========================================");
            System.out.println("Test merchant created:");
            System.out.println("Merchant ID: " + testMerchantId);
            System.out.println("API Key: " + testMerchant.getApiKey());
            System.out.println("API Secret: " + testMerchant.getApiSecret());
            System.out.println("===========================================");
        }
    }
}