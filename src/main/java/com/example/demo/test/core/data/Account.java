package com.example.demo.test.core.data;

import java.util.Currency;

public record Account(
        AccountId id,
        long amount,
        Currency currency,
        Long version
) {
}
