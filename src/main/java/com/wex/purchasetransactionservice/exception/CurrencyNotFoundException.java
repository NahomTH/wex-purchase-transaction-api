package com.wex.purchasetransactionservice.exception;

public class CurrencyNotFoundException extends RuntimeException{
    public CurrencyNotFoundException(String message){
        super(message);
    }
}
