package com.wex.purchasetransactionservice.service;

import com.wex.purchasetransactionservice.client.TreasuryExchangeRateClient;
import com.wex.purchasetransactionservice.exception.CurrencyDataUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private TreasuryExchangeRateClient client;

    @InjectMocks
    private CurrencyService service;

    @Test
    void checkCurrency_validatesAgainstLoadedList() {
        when(client.fetchAllDistinctCurrencies()).thenReturn(Set.of("Canada-Dollar", "United Kingdom-Pound"));

        assertThat(service.checkCurrency("Canada-Dollar")).isTrue();
        assertThat(service.checkCurrency("Junk")).isFalse();
    }

    @Test
    void getAllCountryCurrencies_loadsLazilyOnFirstAccess() {
        when(client.fetchAllDistinctCurrencies()).thenReturn(Set.of("Canada-Dollar"));
        Set<String> result = service.getAllCountryCurrencies();
        assertThat(result).containsExactly("Canada-Dollar");
        verify(client).fetchAllDistinctCurrencies();
    }

    @Test
    void checkCurrency_throwsWhenListUnavailable() {
        when(client.fetchAllDistinctCurrencies()).thenThrow(new RuntimeException("treasury down"));

        assertThatThrownBy(() -> service.checkCurrency("Canada-Dollar"))
                .isInstanceOf(CurrencyDataUnavailableException.class);
    }
}
