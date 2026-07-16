package com.wex.purchasetransactionservice.service;

import com.wex.purchasetransactionservice.model.ExchangeRate;
import com.wex.purchasetransactionservice.client.TreasuryExchangeRateClient;
import com.wex.purchasetransactionservice.model.TreasuryRateResponse;
import com.wex.purchasetransactionservice.exception.ExchangeRateUnAvailableException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ExchangeRateService {

    private final TreasuryExchangeRateClient treasuryExchangeRateClient;

    public ExchangeRateService(TreasuryExchangeRateClient treasuryExchangeRateClient) {
        this.treasuryExchangeRateClient = treasuryExchangeRateClient;
    }

    @Cacheable("exchange-rates")
    public ExchangeRate findApplicableRate(String countryCurrencyDesc, LocalDate purchaseDate) {
        LocalDate startDate = purchaseDate.minusMonths(6);
        List<TreasuryRateResponse.Record> records =
                treasuryExchangeRateClient.fetchRates(startDate, purchaseDate, countryCurrencyDesc);
        if (records == null || records.isEmpty()) {
            throw new ExchangeRateUnAvailableException("No exchange rate available for " + countryCurrencyDesc + " in " + purchaseDate);
        }
        TreasuryRateResponse.Record matchedRate = records.stream().max(Comparator.comparing(TreasuryRateResponse.Record::recordDate))
                .orElseThrow(() -> new ExchangeRateUnAvailableException("No exchange rate available for " + countryCurrencyDesc + " in " + purchaseDate));
        return new ExchangeRate(matchedRate.countryCurrencyDesc(), matchedRate.exchangeRate(), matchedRate.recordDate());
    }
}
