package com.wex.purchasetransactionservice.dto;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


public class Purchase {
    private UUID id;
    private String description;
    private LocalDate transactionDate;
    private BigDecimal amount;

    public Purchase(UUID id, String description, LocalDate transactionDate, BigDecimal amount) {
        this.id = id;
        this.description = description;
        this.transactionDate = transactionDate;
        this.amount = amount;
    }

    public Purchase() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
