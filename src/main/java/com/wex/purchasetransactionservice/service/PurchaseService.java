package com.wex.purchasetransactionservice.service;

import com.wex.purchasetransactionservice.dto.PurchaseRequest;
import com.wex.purchasetransactionservice.entity.Purchase;
import com.wex.purchasetransactionservice.model.ExchangeRate;
import com.wex.purchasetransactionservice.dto.ConvertedPurchase;
import com.wex.purchasetransactionservice.exception.CurrencyNotFoundException;
import com.wex.purchasetransactionservice.exception.PurchaseNotFoundException;
import com.wex.purchasetransactionservice.repository.PurchaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;



    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository, CurrencyService currencyService, ExchangeRateService exchangeRateService) {
        this.purchaseRepository = purchaseRepository;
        this.currencyService = currencyService;
        this.exchangeRateService = exchangeRateService;
    }

    public ConvertedPurchase retrieveConvertedPurchase(
            final UUID id,
            final String currency
    ) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        if(!currencyService.checkCurrency(currency)){
            throw new CurrencyNotFoundException("Currency " +  currency + " not found. please refer /api/v1/all/currencies endpoint to " +
                    "get the list of valid currencies");
        }
        ExchangeRate applicableRate = exchangeRateService.findApplicableRate(currency, purchase.getTransactionDate());
        BigDecimal converted = purchase.getAmountUsd()
                .multiply(applicableRate.exchangeRate())
                .setScale(2, RoundingMode.HALF_EVEN);
        return new ConvertedPurchase(
                purchase.getId(),
                purchase.getDescription(),
                purchase.getTransactionDate(),
                purchase.getAmountUsd(),
                currency,
                applicableRate.recordDate(),
                applicableRate.exchangeRate(),
                converted);
    }

    @Transactional
    public Purchase savePurchase(final PurchaseRequest purchaseRequest) {
        BigDecimal normalizedAmount = purchaseRequest.purchaseAmount().setScale(2, RoundingMode.HALF_EVEN);
        Purchase purchase = new Purchase(UUID.randomUUID(), purchaseRequest.description(), purchaseRequest.transactionDate(), normalizedAmount);

        return purchaseRepository.save(purchase);
    }

    @Transactional
    public Purchase getPurchaseById(final UUID id) {
        return purchaseRepository.findById(id).orElseThrow(() -> new PurchaseNotFoundException(id));
    }

}
