package com.example.demo.test.core.port;

import com.example.demo.test.core.data.Account;
import com.example.demo.test.core.data.AccountId;
import reactor.core.publisher.Mono;

public interface RetrieveAccountPort {

    Mono<Account> findAccount(AccountId id);

}
