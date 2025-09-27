package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.entity.Token;
import com.sabpaisa.tokenization.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    Optional<Token> findByTokenValue(String tokenValue);
    
    Optional<Token> findByCardHashAndMerchant(String cardHash, Merchant merchant);
    
    List<Token> findByMerchant(Merchant merchant);
    
    List<Token> findByMerchantAndStatus(Merchant merchant, String status);
    
    boolean existsByTokenValue(String tokenValue);
}