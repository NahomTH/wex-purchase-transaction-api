package com.wex.purchasetransactionservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI purchaseTransactionOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Wex Purchase Transaction Service API")
                .description("Stores USD purchase transactions and retrieves them converted to a Treasury supported " +
                        "foreign currency uses the most recent exchange within the preceding 6 months.")
                .version("v1"));
    }
}
