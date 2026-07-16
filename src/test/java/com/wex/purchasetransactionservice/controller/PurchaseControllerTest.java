package com.wex.purchasetransactionservice.controller;

import com.wex.purchasetransactionservice.client.TreasuryExchangeRateClient;
import com.wex.purchasetransactionservice.entity.Purchase;
import com.wex.purchasetransactionservice.exception.CurrencyNotFoundException;
import com.wex.purchasetransactionservice.exception.ExchangeRateUnAvailableException;
import com.wex.purchasetransactionservice.exception.PurchaseNotFoundException;
import com.wex.purchasetransactionservice.exception.TreasuryServiceUnavailableException;
import com.wex.purchasetransactionservice.service.PurchaseService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class PurchaseControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PurchaseService purchaseService;

    @MockitoBean
    private TreasuryExchangeRateClient treasuryExchangeRateClient;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void create_returns201WithLocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(purchaseService.savePurchase(any())).thenReturn(
                new Purchase(id, "PS5", LocalDate.parse("2024-05-24"), new BigDecimal("500.89")));

        MvcResult result = mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"PS5","transactionDate":"2024-05-24","purchaseAmount":500.89}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.description").value("PS5"))
                .andReturn();

        assertThat(result.getResponse().getHeader("Location")).endsWith("/api/v1/purchases/" + id);
    }

    @Test
    void create_returns400OnInvalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"","transactionDate":null,"purchaseAmount":-1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.messages[0]").exists());
    }

    @Test
    void create_returns400OnMalformedDate() throws Exception {
        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"PS5","transactionDate":"not-a-date","purchaseAmount":500.89}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").exists());
    }

    @Test
    void getConverted_returns400OnUnknownCurrency() throws Exception {
        when(purchaseService.retrieveConvertedPurchase(any(), any()))
                .thenThrow(new CurrencyNotFoundException("Currency XYZ not found"));

        mockMvc.perform(get("/api/v1/purchases/converted/{id}", UUID.randomUUID())
                        .param("currency", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").exists());
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(purchaseService.getPurchaseById(any())).thenThrow(new PurchaseNotFoundException(id));

        mockMvc.perform(get("/api/v1/purchases/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"));
    }

    @Test
    void getConverted_returns503WithRetryAfterWhenTreasuryDown() throws Exception {
        when(purchaseService.retrieveConvertedPurchase(any(), any()))
                .thenThrow(new TreasuryServiceUnavailableException(
                        "The Treasury exchange rate service is temporarily unavailable. Please try again later.",
                        new RuntimeException("connection refused")));

        mockMvc.perform(get("/api/v1/purchases/converted/{id}", UUID.randomUUID())
                        .param("currency", "Canada-Dollar"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "30"))
                .andExpect(jsonPath("$.status").value("503 SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.messages[0]").exists());
    }

    @Test
    void getConverted_returns422WhenRateUnavaliable() throws Exception {
        when(purchaseService.retrieveConvertedPurchase(any(), any()))
                .thenThrow(new ExchangeRateUnAvailableException("No exchange rate available"));

        mockMvc.perform(get("/api/v1/purchases/converted/{id}", UUID.randomUUID())
                        .param("currency", "Canada-Dollar"))
                .andExpect(status().isUnprocessableEntity());
    }
}
