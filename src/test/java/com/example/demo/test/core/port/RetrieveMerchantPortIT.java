package com.example.demo.test.core.port;

import com.example.demo.test.core.data.AccountId;
import com.example.demo.test.core.data.Merchant;
import com.example.demo.test.core.data.MerchantId;
import com.example.demo.test.infra.adapter.r2dbc.record.AccountRecord;
import com.example.demo.test.infra.adapter.r2dbc.record.MerchantRecord;
import com.example.demo.test.infra.adapter.r2dbc.repository.AccountRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.MerchantRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;

import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RetrieveMerchantPort")
public class RetrieveMerchantPortIT extends AbstractPortIT {

    @Autowired
    RetrieveMerchantPort port;

    @Autowired
    MerchantRecordRepository merchantRecordRepository;

    @Autowired
    AccountRecordRepository accountRecordRepository;

    @Test
    void retrievesWhenExists() {
        var currency = Currency.getInstance("PHP");
        var accountId = new AccountId(UUID.randomUUID().toString());
        var accountRecord = new AccountRecord(accountId.value(), 2000L, currency.getCurrencyCode(), currency.getDefaultFractionDigits(), null);
        accountRecordRepository.save(accountRecord).block();

        var merchantId = new MerchantId(UUID.randomUUID().toString());
        var merchantRecord = new MerchantRecord(merchantId.value(), accountId.value(), null);
        merchantRecordRepository.save(merchantRecord).block();

        var result = port.findMerchant(merchantId).block();

        var expected = new Merchant(merchantId, accountRecord.toAccount());
        assertThat(result).usingRecursiveComparison()
                .ignoringFields("account.version")
                .isEqualTo(expected);
        assertThat(result.account().version()).isEqualTo(0L);
    }

    @Test
    void retrievesWhenNotExists() {
        var merchantId = new MerchantId(UUID.randomUUID().toString());

        var result = port.findMerchant(merchantId).blockOptional();

        assertThat(result).isEmpty();
    }
}
