package com.wex.purchasetransactionservice.service;

import com.wex.purchasetransactionservice.client.TreasuryExchangeRateClient;
import com.wex.purchasetransactionservice.exception.ExchangeRateUnAvailableException;
import com.wex.purchasetransactionservice.model.ExchangeRate;
import com.wex.purchasetransactionservice.model.TreasuryRateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private static final String GBP = "United Kingdom-Pound";

    @Mock
    private TreasuryExchangeRateClient client;

    @InjectMocks
    private ExchangeRateService service;

    private static TreasuryRateResponse.Record rate(String date, String value) {
        return new TreasuryRateResponse.Record(GBP, new BigDecimal(value), LocalDate.parse(date));
    }

    @Test
    void findApplicableRate_returnsMostRecentRate() {
        LocalDate purchase = LocalDate.parse("2024-05-24");
        when(client.fetchRates(purchase.minusMonths(6), purchase, GBP)).thenReturn(List.of(
                rate("2023-12-31", "0.77"),
                rate("2024-03-31", "0.79"),
                rate("2024-01-31", "0.78")
        ));
        ExchangeRate result = service.findApplicableRate(GBP, purchase);
        assertThat(result.recordDate()).isEqualTo("2024-03-31");
        assertThat(result.exchangeRate()).isEqualByComparingTo("0.79");
        assertThat(result.currency()).isEqualTo(GBP);
    }

    @Test
    void findApplicableRate_queriesSixMonthWindow() {
        LocalDate purchase = LocalDate.parse("2024-05-24");
        when(client.fetchRates(any(), any(), eq(GBP))).thenReturn(List.of(rate("2024-03-31", "0.79")));
        service.findApplicableRate(GBP, purchase);
        verify(client).fetchRates(LocalDate.parse("2023-11-24"), purchase, GBP);
    }

    @Test
    void findApplicableRate_picksExactDateMatch() {
        LocalDate purchase = LocalDate.parse("2024-03-31");
        when(client.fetchRates(purchase.minusMonths(6), purchase, GBP)).thenReturn(List.of(
                rate("2023-12-31", "0.77"),
                rate("2024-03-31", "0.79")
        ));
        ExchangeRate result = service.findApplicableRate(GBP, purchase);
        assertThat(result.recordDate()).isEqualTo("2024-03-31");
    }

    @Test
    void findApplicableRate_throwsWhenNoRateInWindow() {
        LocalDate purchase = LocalDate.parse("2024-05-24");
        when(client.fetchRates(purchase.minusMonths(6), purchase, GBP)).thenReturn(List.of());
        assertThatThrownBy(() -> service.findApplicableRate(GBP, purchase))
                .isInstanceOf(ExchangeRateUnAvailableException.class);
    }
}
