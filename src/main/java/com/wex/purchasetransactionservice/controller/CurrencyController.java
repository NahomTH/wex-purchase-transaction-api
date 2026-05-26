package com.wex.purchasetransactionservice.controller;

import com.wex.purchasetransactionservice.service.CurrencyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Currencies", description = "Currencies supported by the Treasury API")
public class CurrencyController {
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/all/currencies")
    public ResponseEntity<Set<String>> getAllCurrencies() {
        return ResponseEntity.ok().body(currencyService.getAllCountryCurrencies());
    }
}
