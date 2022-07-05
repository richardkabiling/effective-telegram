package com.example.demo.test.infra.adapter.r2dbc.record;

import com.example.demo.test.core.data.Tx;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("TXS")
public record TxRecord(@Id String id, Instant txAt) implements Persistable<String> {

    public static TxRecord fromTx(Tx tx) {
        return new TxRecord(tx.id().value().toString(), tx.txAt());
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
