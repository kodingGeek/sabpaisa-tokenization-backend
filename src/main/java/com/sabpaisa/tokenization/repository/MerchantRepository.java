package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    
    Optional<Merchant> findByMerchantId(String merchantId);
    
    Optional<Merchant> findByEmail(String email);
    
    boolean existsByMerchantId(String merchantId);
    
    boolean existsByEmail(String email);
    
    Optional<Merchant> findByApiKey(String apiKey);
    
    Page<Merchant> findByStatus(String status, Pageable pageable);
}