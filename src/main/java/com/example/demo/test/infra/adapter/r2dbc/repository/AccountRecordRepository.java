package com.example.demo.test.infra.adapter.r2dbc.repository;

import com.example.demo.test.infra.adapter.r2dbc.record.AccountRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AccountRecordRepository extends ReactiveCrudRepository<AccountRecord, String> {
}
