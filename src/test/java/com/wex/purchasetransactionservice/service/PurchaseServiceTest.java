package com.wex.purchasetransactionservice.service;

import com.wex.purchasetransactionservice.dto.ConvertedPurchase;
import com.wex.purchasetransactionservice.dto.PurchaseRequest;
import com.wex.purchasetransactionservice.entity.Purchase;
import com.wex.purchasetransactionservice.exception.CurrencyNotFoundException;
import com.wex.purchasetransactionservice.exception.PurchaseNotFoundException;
import com.wex.purchasetransactionservice.model.ExchangeRate;
import com.wex.purchasetransactionservice.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {
    private static final String CAD = "Canada-Dollar";

    @Mock
    private PurchaseRepository repository;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private PurchaseService service;

    @Test
    void savePurchase_roundsAmountToCent() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Purchase saved = service.savePurchase(
                new PurchaseRequest("Coffee", LocalDate.parse("2024-05-24"), new BigDecimal("1.225")));

        assertThat(saved.getAmountUsd()).isEqualByComparingTo("1.22");
        assertThat(saved.getAmountUsd().scale()).isEqualTo(2);
    }

    @Test
    void retrieveConvertedPurchase_appliesRate() {
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.parse("2024-05-24");
        Purchase purchase = new Purchase(id, "PS5", date, new BigDecimal("100.00"));

        when(repository.findById(id)).thenReturn(Optional.of(purchase));
        when(currencyService.checkCurrency(CAD)).thenReturn(true);
        when(exchangeRateService.findApplicableRate(CAD, date))
                .thenReturn(new ExchangeRate(CAD, new BigDecimal("0.79"), LocalDate.parse("2024-05-01")));

        ConvertedPurchase result = service.retrieveConvertedPurchase(id, CAD);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.description()).isEqualTo("PS5");
        assertThat(result.transactionDate()).isEqualTo(date);
        assertThat(result.originalAmountUsd()).isEqualByComparingTo("100.00");
        assertThat(result.targetCurrency()).isEqualTo(CAD);
        assertThat(result.exchangeRate()).isEqualByComparingTo("0.79");
        assertThat(result.exchangeRateDate()).isEqualTo(LocalDate.parse("2024-05-01"));
        assertThat(result.convertedAmount()).isEqualByComparingTo("79.00");
    }

    @Test
    void retrieveConvertedPurchase_throwsOnUnknownCurrency() {
        UUID id = UUID.randomUUID();
        Purchase purchase = new Purchase(id, "test Description", LocalDate.parse("2024-05-24"), new BigDecimal("1.00"));
        when(repository.findById(id)).thenReturn(Optional.of(purchase));
        when(currencyService.checkCurrency("Junk")).thenReturn(false);

        assertThatThrownBy(() -> service.retrieveConvertedPurchase(id, "Junk"))
                .isInstanceOf(CurrencyNotFoundException.class);
    }

    @Test
    void retrieveConvertedPurchase_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveConvertedPurchase(id, CAD))
                .isInstanceOf(PurchaseNotFoundException.class);
    }
}
