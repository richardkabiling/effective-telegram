package com.example.demo.test.infra.adapter.r2dbc;

import com.example.demo.test.core.data.Account;
import com.example.demo.test.core.data.AccountId;
import com.example.demo.test.core.port.RetrieveAccountPort;
import com.example.demo.test.infra.adapter.r2dbc.record.AccountRecord;
import com.example.demo.test.infra.adapter.r2dbc.repository.AccountRecordRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class AccountRepository implements RetrieveAccountPort {

    private final AccountRecordRepository repository;

    public AccountRepository(AccountRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Account> findAccount(AccountId id) {
        return repository.findById(id.value())
                .map(AccountRecord::toAccount);
    }
}
