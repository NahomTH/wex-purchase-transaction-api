package com.wex.purchasetransactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConvertedPurchase(
        UUID id,
        String description,
        LocalDate transactionDate,
        BigDecimal originalAmountUsd,
        String targetCurrency,
        LocalDate exchangeRateDate,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount) {
}