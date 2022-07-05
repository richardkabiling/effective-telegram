package com.example.demo.test.core.config;

import com.example.demo.test.core.data.PaymentId;
import com.example.demo.test.core.data.TxEntryId;
import com.example.demo.test.core.service.PaymentIdGenerationStrategy;
import com.example.demo.test.core.service.TxEntryIdGenerationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

@Configuration
public class PaymentServiceConfiguration {

    @Bean
    public PaymentIdGenerationStrategy paymentIdGenerationStrategy() {
        return () -> new PaymentId(UUID.randomUUID());
    }

    @Bean
    public TxEntryIdGenerationStrategy txEntryIdGenerationStrategy() {
        return () -> new TxEntryId(UUID.randomUUID());
    }

    @Bean
    public Supplier<Clock> clockSupplier() {
        return Clock::systemUTC;
    }
}
