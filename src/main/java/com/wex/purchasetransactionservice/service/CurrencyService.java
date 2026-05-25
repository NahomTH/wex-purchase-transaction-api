package com.wex.purchasetransactionservice.service;

import com.wex.purchasetransactionservice.client.TreasuryExchangeRateClient;
import com.wex.purchasetransactionservice.exception.CurrencyDataUnavailableException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class CurrencyService {

    private final TreasuryExchangeRateClient treasuryClient;

    private volatile Set<String> currencyList = Set.of();

    CurrencyService(TreasuryExchangeRateClient treasuryExchangeRateClient) {
        this.treasuryClient = treasuryExchangeRateClient;
    }

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Chicago")
    public void refresh() {
        try {
            Set<String> fetched = treasuryClient.fetchAllDistinctCurrencies();
            if (fetched != null && !fetched.isEmpty()) {
                currencyList = fetched;
                log.info("Loaded {} distinct currencies from Treasury", fetched.size());
            }
        } catch (Exception e) {
            log.warn("Failed to refresh currency list from Treasury; keeping {} cached entries",
                    currencyList.size(), e);
        }
    }

    public Set<String> getAllCountryCurrencies() {
        ensureLoaded();
        return currencyList;
    }

    public Boolean checkCurrency(String currency) {
        ensureLoaded();
        if (currencyList.isEmpty()) {
            throw new CurrencyDataUnavailableException(
                    "Supported currency list is unavailable; the Treasury API could not be reached");
        }
        return currencyList.contains(currency);
    }

    private synchronized void ensureLoaded() {
        if (currencyList.isEmpty()) {
            refresh();
        }
    }
}
