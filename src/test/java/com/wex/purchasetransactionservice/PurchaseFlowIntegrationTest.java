package com.wex.purchasetransactionservice;

import com.wex.purchasetransactionservice.client.TreasuryExchangeRateClient;
import com.wex.purchasetransactionservice.model.TreasuryRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class PurchaseFlowIntegrationTest {

    private static final String CAD = "Canada-Dollar";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TreasuryExchangeRateClient treasuryClient;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void postPurchase_thenGetConverted_returnsConvertedAmount() throws Exception {
        when(treasuryClient.fetchAllDistinctCurrencies()).thenReturn(Set.of(CAD));
        when(treasuryClient.fetchRates(any(), any(), eq(CAD))).thenReturn(List.of(
                new TreasuryRateResponse.Record(CAD, new BigDecimal("1.35"), LocalDate.parse("2024-05-01"))));

        MvcResult created = mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"PS5","transactionDate":"2024-05-24","purchaseAmount":100.00}"""))
                .andExpect(status().isCreated())
                .andReturn();

        String location = created.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        String id = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(get("/api/v1/purchases/converted/{id}", id).param("currency", CAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.originalAmountUsd").value(100.00))
                .andExpect(jsonPath("$.exchangeRate").value(1.35))
                .andExpect(jsonPath("$.convertedAmount").value(135.00))
                .andExpect(jsonPath("$.targetCurrency").value(CAD));
    }
}
