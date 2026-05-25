package com.wex.purchasetransactionservice.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRate(
        String currency,
        BigDecimal exchangeRate,
        LocalDate recordDate
) {
}
