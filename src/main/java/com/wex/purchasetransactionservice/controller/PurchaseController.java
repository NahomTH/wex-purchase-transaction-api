package com.wex.purchasetransactionservice.controller;

import com.wex.purchasetransactionservice.dto.PurchaseRequest;
import com.wex.purchasetransactionservice.dto.PurchaseResponse;
import com.wex.purchasetransactionservice.dto.ConvertedPurchase;
import com.wex.purchasetransactionservice.entity.Purchase;
import com.wex.purchasetransactionservice.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Purchases", description = "Store purchase transactions and retrieve them converted to a foreign currency")
public class PurchaseController {
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Operation(summary = "Store a purchase transaction",
            description = "Persists a USD purchase and assigns a unique identifier")
    @PostMapping("/purchases")
    public ResponseEntity<PurchaseResponse> create(@Valid @RequestBody PurchaseRequest purchaseRequest) {
        Purchase purchase = purchaseService.savePurchase(purchaseRequest);
        URI location = UriComponentsBuilder
                .fromPath("/api/v1/purchases/{id}")
                .buildAndExpand(purchase.getId())
                .toUri();
        return ResponseEntity.created(location).body(PurchaseResponse.from(purchase));
    }

    @Operation(summary = "Retrieve a stored purchase by id")
    @GetMapping("/purchases/{id}")
    public ResponseEntity<PurchaseResponse> getResponseById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(PurchaseResponse.from(purchaseService.getPurchaseById(id)));
    }

    @Operation(summary = "Retrieve a stored purchase converted to a target currency",
            description = "Converts the stored USD amount using the most recent Treasury exchange rate")
    @GetMapping("/purchases/converted/{id}")
    public ResponseEntity<ConvertedPurchase> getPurchaseByCurrencyType(
            @PathVariable final UUID id,
            @Parameter(description = "Treasury country_currency_desc value", example = "Canada-Dollar")
            @RequestParam(value = "currency") final String currency
    ) {
        return ResponseEntity.ok().body(purchaseService.retrieveConvertedPurchase(id, currency));
    }


}
