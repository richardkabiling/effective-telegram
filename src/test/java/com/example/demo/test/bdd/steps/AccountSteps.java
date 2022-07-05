package com.example.demo.test.bdd.steps;

import com.example.demo.test.infra.adapter.r2dbc.record.AccountRecord;
import com.example.demo.test.infra.adapter.r2dbc.repository.AccountRecordRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

import static com.example.demo.test.app.adapter.controller.Amounts.toAdjustedAmount;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountSteps {

    @Autowired
    AccountRecordRepository repository;

    @After(order = 10)
    public void tearDown() {
        repository.deleteAll().block();
    }

    @Given("the following accounts exist:")
    public void theFollowingAccountsExist(DataTable dataTable) {
        var accountRecords = dataTable.entries()
                .stream()
                .map(this::toAccountRecord)
                .toList();
        repository.saveAll(accountRecords).collectList()
                .block();
    }

    private AccountRecord toAccountRecord(Map<String, String> entry) {
        var id = entry.get("id");
        var amount = new BigDecimal(entry.get("balance"));
        var currency = Currency.getInstance(entry.get("currency"));
        return new AccountRecord(
                id,
                toAdjustedAmount(currency, amount),
                currency.getCurrencyCode(),
                currency.getDefaultFractionDigits(),
                null
        );
    }

    @Then("the account {string} balance is {word} {bigdecimal}")
    public void theAccountBalanceIsAsserted(String accountId, String currency, BigDecimal amount) {
        var record = repository.findById(accountId).block();
        var savedAmount = record.amount();
        var expectedAmount = toAdjustedAmount(Currency.getInstance(currency), amount);

        assertThat(savedAmount).isEqualTo(expectedAmount);
    }
}
