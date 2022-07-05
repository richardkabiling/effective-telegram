package com.example.demo.test.core.service;

import com.example.demo.test.core.data.PaymentId;

import java.util.function.Supplier;

public interface PaymentIdGenerationStrategy extends Supplier<PaymentId> {
}
