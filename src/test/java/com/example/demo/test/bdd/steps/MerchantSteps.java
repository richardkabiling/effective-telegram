package com.example.demo.test.bdd.steps;

import com.example.demo.test.infra.adapter.r2dbc.record.MerchantRecord;
import com.example.demo.test.infra.adapter.r2dbc.repository.MerchantRecordRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class MerchantSteps {

    @Autowired
    MerchantRecordRepository repository;

    @After(order = 20)
    public void tearDown() {
        repository.deleteAll().block();
    }

    @Given("the following merchants exist:")
    public void theFollowingMerchantsExist(DataTable dataTable) {
        var merchantRecords = dataTable.entries()
                .stream()
                .map(this::toMerchantRecords)
                .toList();
        repository.saveAll(merchantRecords).collectList()
                .block();
    }

    private MerchantRecord toMerchantRecords(Map<String, String> entry) {
        var id = entry.get("id");
        var accountId = entry.get("account id");
        return new MerchantRecord(
                id,
                accountId,
                null
        );
    }

}
