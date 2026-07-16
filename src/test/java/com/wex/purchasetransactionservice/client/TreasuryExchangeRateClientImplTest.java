package com.wex.purchasetransactionservice.client;

import com.wex.purchasetransactionservice.exception.TreasuryServiceUnavailableException;
import com.wex.purchasetransactionservice.model.TreasuryRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TreasuryExchangeRateClientImplTest {
    private static final String CAD = "Canada-Dollar";
    private static final String RATES_PATH = "/v1/accounting/od/rates_of_exchange";
    private MockRestServiceServer server;
    private TreasuryExchangeRateClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://treasury.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TreasuryExchangeRateClientImpl(builder.build(), 10);
        ReflectionTestUtils.setField(client, "ratesPath", RATES_PATH);
    }

    @Test
    void fetchRates_buildsFilterAndParsesResponse() {
        server.expect(request -> {
            String uri = URLDecoder.decode(request.getURI().toString(), StandardCharsets.UTF_8);
            assertThat(uri).contains(RATES_PATH);
            assertThat(uri).contains("country_currency_desc:in:(Canada-Dollar)");
            assertThat(uri).contains("record_date:gte:2024-01-15");
            assertThat(uri).contains("record_date:lte:2024-07-15");
            assertThat(uri).contains("sort=-record_date");
        }).andRespond(withSuccess("""
                {"data":[{"country_currency_desc":"Canada-Dollar","exchange_rate":"1.35","record_date":"2024-06-30"}],
                 "meta":{"total-count":1,"total-pages":1}}""", MediaType.APPLICATION_JSON));

        List<TreasuryRateResponse.Record> records =
                client.fetchRates(LocalDate.parse("2024-01-15"), LocalDate.parse("2024-07-15"), CAD);

        assertThat(records).hasSize(1);
        assertThat(records.getFirst().recordDate()).isEqualTo("2024-06-30");
        assertThat(records.getFirst().exchangeRate()).isEqualByComparingTo("1.35");
        assertThat(records.getFirst().countryCurrencyDesc()).isEqualTo(CAD);
        server.verify();
    }

    @Test
    void fetchRates_throwsTreasuryServiceUnavailableWhenTreasuryFails() {
        server.expect(anything()).andRespond(withServerError());

        assertThatThrownBy(() ->
                client.fetchRates(LocalDate.parse("2024-01-15"), LocalDate.parse("2024-07-15"), CAD))
                .isInstanceOf(TreasuryServiceUnavailableException.class)
                .hasMessageContaining("temporarily unavailable");
    }

    @Test
    void fetchAllDistinctCurrencies_throwsTreasuryServiceUnavailableWhenTreasuryFails() {
        server.expect(anything()).andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchAllDistinctCurrencies())
                .isInstanceOf(TreasuryServiceUnavailableException.class)
                .hasMessageContaining("temporarily unavailable");
    }
}
