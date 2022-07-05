package com.example.demo.test.infra.adapter.r2dbc.record;

import com.example.demo.test.core.data.Account;
import com.example.demo.test.core.data.AccountId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Currency;

@Table("ACCOUNTS")
public record AccountRecord(
    @Id String id,
    long amount,
    String currency,
    int currencyMinorUnits,
    @Version Long version
) {

    public Account toAccount() {
        return new Account(
                new AccountId(id),
                amount,
                Currency.getInstance(currency),
                version
        );
    }

    public static AccountRecord fromAccount(Account account) {
        return new AccountRecord(
                account.id().value(),
                account.amount(),
                account.currency().getCurrencyCode(),
                account.currency().getDefaultFractionDigits(),
                account.version()
        );
    }
}
