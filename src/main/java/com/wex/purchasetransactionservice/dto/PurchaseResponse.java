package com.wex.purchasetransactionservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseResponse(
        UUID id,
        String description,
        LocalDate transactionDate,
        BigDecimal amount
) {
    public static PurchaseResponse from(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getDescription(),
                purchase.getTransactionDate(),
                purchase.getAmount());
    }
}
