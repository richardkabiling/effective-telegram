package com.example.demo.test.core.data;

import java.util.Currency;

public record TxEntry(
        TxEntryId id,
        TxId txId,
        TxEntryType type,
        AccountId accountId,
        long amount,
        Currency currency
) {
}
