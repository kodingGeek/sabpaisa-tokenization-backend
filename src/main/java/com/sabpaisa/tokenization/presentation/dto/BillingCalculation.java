package com.sabpaisa.tokenization.presentation.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BillingCalculation {
    private BigDecimal tokenCreationCharges;
    private BigDecimal storageCharges;
    private BigDecimal transactionCharges;
    private BigDecimal platformCharges;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
}