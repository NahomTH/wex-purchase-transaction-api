package com.wex.purchasetransactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PurchaseTransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PurchaseTransactionServiceApplication.class, args);
    }

}
