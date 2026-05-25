package com.wex.purchasetransactionservice.repository;

import com.wex.purchasetransactionservice.dto.Purchase;
import com.wex.purchasetransactionservice.entity.PurchaseEntity;
import org.springframework.stereotype.Component;

@Component
public class PurchaseEntityMapper {

    public PurchaseEntity toEntity(Purchase purchase) {
        return new PurchaseEntity(
                purchase.getId(),
                purchase.getDescription(),
                purchase.getTransactionDate(),
                purchase.getAmount()
        );
    }
    public Purchase toDomain(PurchaseEntity entity) {
        return new Purchase(
                entity.getId(),
                entity.getDescription(),
                entity.getTransactionDate(),
                entity.getAmountUsd()
        );
    }
}