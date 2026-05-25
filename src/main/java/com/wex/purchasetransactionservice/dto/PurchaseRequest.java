package com.wex.purchasetransactionservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequest(
        @Schema(description = "Free-text description, at most 50 characters", example = "Transaction for buying PS5")
        @NotBlank
        @Size(max = 50, message = "Description must not exceed 50 characters")
        String description,
        @Schema(description = "Date of the purchase (yyyy-MM-dd)", example = "2024-05-24")
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate transactionDate,
        @Schema(description = "Purchase amount in USD; rounded to the nearest cent on storage", example = "500.89")
        @NotNull
        @DecimalMin(value = "0.01", message = "Purchase amount must be a positive value of at least 0.01")
        @DecimalMax(value = "99999999999999999.99", message = "Purchase amount is too large")
        BigDecimal purchaseAmount
) {
}