package com.wex.purchasetransactionservice.repository;

import com.wex.purchasetransactionservice.client.TreasuryExchangeRateClient;
import com.wex.purchasetransactionservice.entity.Purchase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseRepositoryTest {

    @Autowired
    private PurchaseRepository repository;

    @MockitoBean
    private TreasuryExchangeRateClient treasuryExchangeRateClient;

    @Test
    void save_thenFindById_returnsPurchase() {
        UUID id = UUID.randomUUID();
        repository.save(new Purchase(id, "PS5", LocalDate.parse("2024-05-24"), new BigDecimal("12.34")));

        Optional<Purchase> found = repository.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("PS5");
        assertThat(found.get().getTransactionDate()).isEqualTo(LocalDate.parse("2024-05-24"));
        assertThat(found.get().getAmountUsd()).isEqualByComparingTo("12.34");
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }
}
