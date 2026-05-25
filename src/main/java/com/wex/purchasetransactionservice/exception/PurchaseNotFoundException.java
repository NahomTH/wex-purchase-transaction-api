package com.wex.purchasetransactionservice.exception;

import java.util.UUID;

public class PurchaseNotFoundException extends RuntimeException {
    public PurchaseNotFoundException(UUID id) {
        super("Purchase with id " + id + " not found");
    }
}
