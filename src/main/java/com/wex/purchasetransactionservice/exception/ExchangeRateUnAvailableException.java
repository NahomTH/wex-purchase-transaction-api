package com.wex.purchasetransactionservice.exception;

public class ExchangeRateUnAvailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String msg;

    public ExchangeRateUnAvailableException(final String msg) {
        super(msg);
        this.msg = msg;
    }
}
