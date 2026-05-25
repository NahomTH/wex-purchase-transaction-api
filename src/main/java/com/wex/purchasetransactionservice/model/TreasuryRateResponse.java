package com.wex.purchasetransactionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TreasuryRateResponse(
        List<Record> data,
        Meta meta) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Record(
            @JsonProperty("country_currency_desc") String countryCurrencyDesc,
            @JsonProperty("exchange_rate") BigDecimal exchangeRate,
            @JsonProperty("record_date") LocalDate recordDate) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            @JsonProperty("total-count") int totalCount,
            @JsonProperty("total-pages") int totalPages) {
    }
}
