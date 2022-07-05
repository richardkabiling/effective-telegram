package com.example.demo.test.core.data;

import java.util.UUID;

public record PaymentId(UUID value) implements TxId {
}
