package com.wex.purchasetransactionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class TreasuryClientConfig {

    @Bean
    public RestClient treasuryRestClient(
            @Value("${treasury.api.base-url:https://api.fiscaldata.treasury.gov/services/api/fiscal_service}") String baseUrl,
            @Value("${treasury.api.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${treasury.api.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
