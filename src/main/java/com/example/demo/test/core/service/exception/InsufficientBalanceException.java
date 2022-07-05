package com.example.demo.test.core.service.exception;

import java.util.Currency;

public class InsufficientBalanceException extends RuntimeException {
    private final Currency currency;
    private final long accountBalance;
    private final long amount;

    public InsufficientBalanceException(Currency currency, long accountBalance, long amount) {
        this.currency = currency;
        this.accountBalance = accountBalance;
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getAccountBalance() {
        return accountBalance;
    }

    public long getAmount() {
        return amount;
    }
}
