package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.domain.entity.Platform;
import com.sabpaisa.tokenization.domain.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {
    
    Optional<Platform> findByPlatformCode(String platformCode);
    
    List<Platform> findByMerchantAndIsActiveTrue(Merchant merchant);
    
    boolean existsByPlatformCode(String platformCode);
}