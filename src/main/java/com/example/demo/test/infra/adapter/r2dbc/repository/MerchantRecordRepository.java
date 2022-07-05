package com.example.demo.test.infra.adapter.r2dbc.repository;

import com.example.demo.test.infra.adapter.r2dbc.record.MerchantRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MerchantRecordRepository extends ReactiveCrudRepository<MerchantRecord, String> {
}
