package com.example.demo.test.core.data;

import java.time.Instant;
import java.util.Currency;
import java.util.Set;

public record Payment(
        PaymentId id,
        Account source,
        Merchant merchant,
        long amount,
        Currency currency,
        Set<TxEntry> entries,
        Instant paidAt
) implements Tx {
    @Override
    public Instant txAt() {
        return paidAt;
    }
}
