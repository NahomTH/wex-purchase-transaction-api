package com.wex.purchasetransactionservice.client;

import com.wex.purchasetransactionservice.exception.TreasuryServiceUnavailableException;
import com.wex.purchasetransactionservice.model.TreasuryRateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.*;


@Slf4j
@Component
public class TreasuryExchangeRateClientImpl implements TreasuryExchangeRateClient {

    private final RestClient treasuryRestClient;
    private final int pageSize;

    public  TreasuryExchangeRateClientImpl(RestClient treasuryRestClient, @Value("${treasury.api.page-size}") int pageSize) {
        this.treasuryRestClient = treasuryRestClient;
        this.pageSize = pageSize;
    }

    @Value("${treasury.api.path:/v1/accounting/od/rates_of_exchange}")
    private String ratesPath;


    @Override
    public List<TreasuryRateResponse.Record> fetchRates(LocalDate startDate, LocalDate purchaseDate, String countryDesc) {
        List<TreasuryRateResponse.Record> allRecords = new ArrayList<>();
        int page = 1, totalPages;
        do {
            TreasuryRateResponse response = fetchPage(startDate, purchaseDate, page, countryDesc);
            if (response == null || response.data() == null) {
                break;
            }
            allRecords.addAll(response.data());
            totalPages = response.meta() != null ? response.meta().totalPages() : 1;
            page++;
        } while (page <= totalPages);
        return allRecords;
    }

    private TreasuryRateResponse fetchPage(LocalDate startDate, LocalDate endDate, int page, String countryDesc) {
        String filter = "country_currency_desc:in:(%s),record_date:gte:%s,record_date:lte:%s".formatted(countryDesc, startDate, endDate);
        try {
            return treasuryRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ratesPath)
                            .queryParam("fields", "country_currency_desc,exchange_rate,record_date")
                            .queryParam("filter", filter)
                            .queryParam("sort", "-record_date")
                            .queryParam("page[number]", page)
                            .queryParam("page[size]", pageSize)
                            .build())
                    .retrieve()
                    .body(TreasuryRateResponse.class);
        } catch (RestClientException e) {
            log.warn("Treasury API call failed fetching rates for {} ({} to {}), page {}: {}",
                    countryDesc, startDate, endDate, page, e.getMessage());
            throw new TreasuryServiceUnavailableException(
                    "The Treasury exchange rate service is temporarily unavailable. Please try again later.", e);
        }
    }

    @Override
    public Set<String> fetchAllDistinctCurrencies() {
        int pageSize = 10000;
        int pageNumber = 1;
        Set<String> distinct = new HashSet<>();
        while (true) {
            int finalPageNumber = pageNumber;
            TreasuryRateResponse response;
            try {
                response = treasuryRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(ratesPath)
                                .queryParam("fields", "country_currency_desc")
                                .queryParam("page[size]", pageSize)
                                .queryParam("page[number]", finalPageNumber)
                                .build())
                        .retrieve()
                        .body(TreasuryRateResponse.class);
            } catch (RestClientException e) {
                log.warn("Treasury API call failed fetching distinct currencies, page {}: {}",
                        finalPageNumber, e.getMessage());
                throw new TreasuryServiceUnavailableException(
                        "The Treasury exchange rate service is temporarily unavailable. Please try again later.", e);
            }

            if (response == null || response.data() == null || response.data().isEmpty()) {
                break;
            }
            response.data().forEach(rateRecord -> distinct.add(rateRecord.countryCurrencyDesc()));
            if (response.data().size() < pageSize) {
                break;
            }
            pageNumber++;
        }
        return Set.copyOf(distinct);
    }
}