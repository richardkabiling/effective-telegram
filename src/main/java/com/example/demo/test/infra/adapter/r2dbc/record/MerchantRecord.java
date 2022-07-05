package com.example.demo.test.infra.adapter.r2dbc.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table("MERCHANTS")
public record MerchantRecord(@Id String id, String accountId, @Version Long version) {
}
