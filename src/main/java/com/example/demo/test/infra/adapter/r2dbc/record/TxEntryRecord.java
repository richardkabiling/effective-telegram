package com.example.demo.test.infra.adapter.r2dbc.record;

import com.example.demo.test.core.data.TxEntry;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table("TX_ENTRIES")
public record TxEntryRecord(
        @Id String id,
        String txId,
        String type,
        String accountId,
        long amount,
        String currency,
        int currencyMinorUnits
) implements Persistable<String> {

    public static TxEntryRecord fromTxEntry(TxEntry txEntry) {
        return new TxEntryRecord(
                txEntry.id().value().toString(),
                txEntry.txId().value().toString(),
                txEntry.type().toString(),
                txEntry.accountId().value(),
                txEntry.amount(),
                txEntry.currency().getCurrencyCode(),
                txEntry.currency().getDefaultFractionDigits());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
