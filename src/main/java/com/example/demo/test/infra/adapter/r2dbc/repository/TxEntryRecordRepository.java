package com.example.demo.test.infra.adapter.r2dbc.repository;

import com.example.demo.test.infra.adapter.r2dbc.record.TxEntryRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface TxEntryRecordRepository extends ReactiveCrudRepository<TxEntryRecord, UUID> {

    Flux<TxEntryRecord> findByTxId(UUID txId);

}
