package com.example.demo.test.app.adapter.controller.data;

import com.example.demo.test.app.adapter.controller.Amounts;
import com.example.demo.test.core.data.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.example.demo.test.app.adapter.controller.Amounts.*;

public record PaymentResponse(
        UUID id,
        String sourceId,
        String merchantId,
        BigDecimal amount,
        String currency,
        Instant paidAt
) {

    public static PaymentResponse fromPayment(Payment payment) {
        return new PaymentResponse(
            payment.id().value(),
            payment.source().id().value(),
            payment.merchant().id().value(),
            fromAdjustedAmount(payment.currency(), payment.amount()),
            payment.currency().getCurrencyCode(),
            payment.paidAt()
        );
    }

}
