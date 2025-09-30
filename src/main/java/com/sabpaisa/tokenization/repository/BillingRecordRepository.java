package com.sabpaisa.tokenization.repository;

import com.sabpaisa.tokenization.domain.entity.BillingRecord;
import com.sabpaisa.tokenization.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {
    
    List<BillingRecord> findByMerchantOrderByBillingMonthDesc(Merchant merchant);
    
    Optional<BillingRecord> findByMerchantAndBillingMonth(Merchant merchant, LocalDate billingMonth);
    
    List<BillingRecord> findByStatusAndDueDateBefore(BillingRecord.BillingStatus status, LocalDate date);
}