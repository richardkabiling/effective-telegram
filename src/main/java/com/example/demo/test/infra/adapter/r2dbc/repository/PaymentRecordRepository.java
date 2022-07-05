package com.example.demo.test.infra.adapter.r2dbc.repository;

import com.example.demo.test.infra.adapter.r2dbc.record.PaymentRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface PaymentRecordRepository extends ReactiveCrudRepository<PaymentRecord, UUID> {
}
