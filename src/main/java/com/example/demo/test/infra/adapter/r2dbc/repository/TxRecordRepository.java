package com.example.demo.test.infra.adapter.r2dbc.repository;

import com.example.demo.test.infra.adapter.r2dbc.record.TxRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface TxRecordRepository extends ReactiveCrudRepository<TxRecord, UUID> {
}
