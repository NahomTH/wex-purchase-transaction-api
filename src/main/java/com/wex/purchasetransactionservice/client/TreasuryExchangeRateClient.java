package com.wex.purchasetransactionservice.client;

import com.wex.purchasetransactionservice.model.TreasuryRateResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface TreasuryExchangeRateClient {

    List<TreasuryRateResponse.Record> fetchRates(LocalDate startDate, LocalDate endDate, String countryDesc);

    Set<String> fetchAllDistinctCurrencies();
}