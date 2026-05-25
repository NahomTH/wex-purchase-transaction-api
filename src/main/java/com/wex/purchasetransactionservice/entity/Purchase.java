package com.wex.purchasetransactionservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "purchase")
public class Purchase {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "amount_usd", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountUsd;

    public Purchase(UUID id, String description, LocalDate localDate, BigDecimal amount) {
        this.id = id;
        this.description = description;
        this.transactionDate = localDate;
        this.amountUsd = amount;
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

    public BigDecimal getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(BigDecimal amountUsd) {
        this.amountUsd = amountUsd;
    }
}
