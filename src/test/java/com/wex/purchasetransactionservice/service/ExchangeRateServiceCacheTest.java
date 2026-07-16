package com.wex.purchasetransactionservice.service;

import com.wex.purchasetransactionservice.client.TreasuryExchangeRateClient;
import com.wex.purchasetransactionservice.exception.ExchangeRateUnAvailableException;
import com.wex.purchasetransactionservice.exception.TreasuryServiceUnavailableException;
import com.wex.purchasetransactionservice.model.ExchangeRate;
import com.wex.purchasetransactionservice.model.TreasuryRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ExchangeRateServiceCacheTest {

    private static final String CAD = "Canada-Dollar";
    private static final String GBP = "United Kingdom-Pound";
    private static final LocalDate PURCHASE_DATE = LocalDate.parse("2024-05-24");

    @Autowired
    private ExchangeRateService service;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private TreasuryExchangeRateClient treasuryClient;

    @BeforeEach
    void clearCache() {
        Objects.requireNonNull(cacheManager.getCache("exchange-rates")).clear();
    }

    private static TreasuryRateResponse.Record rate(String currency, String value) {
        return new TreasuryRateResponse.Record(currency, new BigDecimal(value), LocalDate.parse("2024-03-31"));
    }

    @Test
    void findApplicableRate_hitsTreasuryOnlyOnceForSameCurrencyAndDate() {
        when(treasuryClient.fetchRates(any(), any(), eq(CAD)))
                .thenReturn(List.of(rate(CAD, "1.35")));

        ExchangeRate first = service.findApplicableRate(CAD, PURCHASE_DATE);
        ExchangeRate second = service.findApplicableRate(CAD, PURCHASE_DATE);

        assertThat(first).isEqualTo(second);
        verify(treasuryClient, times(1)).fetchRates(any(), any(), eq(CAD));
    }

    @Test
    void findApplicableRate_cachesPerCurrencyAndDate() {
        when(treasuryClient.fetchRates(any(), any(), eq(CAD))).thenReturn(List.of(rate(CAD, "1.35")));
        when(treasuryClient.fetchRates(any(), any(), eq(GBP))).thenReturn(List.of(rate(GBP, "0.79")));

        service.findApplicableRate(CAD, PURCHASE_DATE);
        service.findApplicableRate(GBP, PURCHASE_DATE);
        service.findApplicableRate(CAD, PURCHASE_DATE.minusDays(1));

        verify(treasuryClient, times(2)).fetchRates(any(), any(), eq(CAD));
        verify(treasuryClient, times(1)).fetchRates(any(), any(), eq(GBP));
    }

    @Test
    void findApplicableRate_doesNotCacheTreasuryFailures() {
        when(treasuryClient.fetchRates(any(), any(), eq(CAD)))
                .thenThrow(new TreasuryServiceUnavailableException("down", new RuntimeException()))
                .thenReturn(List.of(rate(CAD, "1.35")));

        assertThatThrownBy(() -> service.findApplicableRate(CAD, PURCHASE_DATE))
                .isInstanceOf(TreasuryServiceUnavailableException.class);

        ExchangeRate recovered = service.findApplicableRate(CAD, PURCHASE_DATE);
        assertThat(recovered.exchangeRate()).isEqualByComparingTo("1.35");
        verify(treasuryClient, times(2)).fetchRates(any(), any(), eq(CAD));
    }

    @Test
    void findApplicableRate_doesNotCacheEmptyRateWindows() {
        when(treasuryClient.fetchRates(any(), any(), eq(CAD)))
                .thenReturn(List.of())
                .thenReturn(List.of(rate(CAD, "1.35")));

        assertThatThrownBy(() -> service.findApplicableRate(CAD, PURCHASE_DATE))
                .isInstanceOf(ExchangeRateUnAvailableException.class);

        ExchangeRate recovered = service.findApplicableRate(CAD, PURCHASE_DATE);
        assertThat(recovered.exchangeRate()).isEqualByComparingTo("1.35");
        verify(treasuryClient, times(2)).fetchRates(any(), any(), eq(CAD));
    }
}
