package com.example.demo.test.app.adapter.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class Amounts {
    public static BigDecimal fromAdjustedAmount(Currency currency, long amount) {
        return new BigDecimal(amount)
                .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN.pow(currency.getDefaultFractionDigits()), RoundingMode.HALF_UP);
    }

    public static long toAdjustedAmount(Currency currency, BigDecimal amount) {
        return amount.multiply(BigDecimal.TEN.pow(currency.getDefaultFractionDigits()))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }
}
