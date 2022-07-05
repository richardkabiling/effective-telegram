package com.example.demo.test.core.port;

import com.example.demo.test.core.data.Account;
import com.example.demo.test.core.data.AccountId;
import com.example.demo.test.infra.adapter.r2dbc.record.AccountRecord;
import com.example.demo.test.infra.adapter.r2dbc.repository.AccountRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;

import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RetrieveAccountPort")
public class RetrieveAccountPortIT extends AbstractPortIT {

    @Autowired
    RetrieveAccountPort port;

    @Autowired
    AccountRecordRepository repository;

    @Test
    void retrievesWhenExists() {
        var currency = Currency.getInstance("PHP");
        var id = new AccountId(UUID.randomUUID().toString());
        var record = new AccountRecord(id.value(), 2000L, currency.getCurrencyCode(), currency.getDefaultFractionDigits(), null);
        repository.save(record).block();

        var result = port.findAccount(id).block();

        var expected = new Account(id, 2000L, currency, 0L);
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void retrievesWhenNotExists() {
        var id = new AccountId(UUID.randomUUID().toString());

        var result = port.findAccount(id).blockOptional();

        assertThat(result).isEmpty();
    }

}
