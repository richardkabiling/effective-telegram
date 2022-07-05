package com.example.demo.test.infra.adapter.r2dbc.record;

import com.example.demo.test.core.data.Payment;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table("PAYMENTS")
public record PaymentRecord(
        @Id String id,
        String sourceId,
        String merchantId,
        long amount,
        String currency,
        int currencyMinorUnits
) implements Persistable<String> {
    public static PaymentRecord fromPayment(Payment payment) {
        return new PaymentRecord(
                payment.id().value().toString(),
                payment.source().id().value(),
                payment.merchant().id().value(),
                payment.amount(),
                payment.currency().getCurrencyCode(),
                payment.currency().getDefaultFractionDigits());
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
