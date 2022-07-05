package com.example.demo.test.core.service.exception;

import java.util.Currency;

public class InconsistentCurrencyException extends RuntimeException {
    private final Currency currency;

    public InconsistentCurrencyException(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }
}
