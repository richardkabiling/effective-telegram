package com.example.demo.test.core.port;

import com.example.demo.test.core.data.*;
import com.example.demo.test.infra.adapter.r2dbc.record.*;
import com.example.demo.test.infra.adapter.r2dbc.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SavePaymentPort")
class SavePaymentPortIT extends AbstractPortIT {

    @Autowired
    SavePaymentPort port;

    @Autowired
    AccountRecordRepository accountRecordRepository;

    @Autowired
    MerchantRecordRepository merchantRecordRepository;

    @Autowired
    TxRecordRepository txRecordRepository;

    @Autowired
    PaymentRecordRepository paymentRecordRepository;

    @Autowired
    TxEntryRecordRepository txEntryRecordRepository;

    @Test
    public void savesPayment() {
        var currency = Currency.getInstance("PHP");

        var sourceAccountId = new AccountId(UUID.randomUUID().toString());
        var sourceAccountRecord = new AccountRecord(sourceAccountId.value(), 2000L, currency.getCurrencyCode(), currency.getDefaultFractionDigits(), null);
        accountRecordRepository.save(sourceAccountRecord).block();

        var merchantAccountId = new AccountId(UUID.randomUUID().toString());
        var merchantAccountRecord = new AccountRecord(merchantAccountId.value(), 2500L, currency.getCurrencyCode(), currency.getDefaultFractionDigits(), null);
        accountRecordRepository.save(merchantAccountRecord).block();

        var merchantId = new MerchantId(UUID.randomUUID().toString());
        var merchantRecord = new MerchantRecord(merchantId.value(), merchantAccountId.value(), null);
        merchantRecordRepository.save(merchantRecord).block();

        var paymentId = new PaymentId(UUID.randomUUID());
        var sourceAccount = new Account(sourceAccountId, 2500L, currency, 0L);
        var merchantAccount = new Account(merchantAccountId, 3000L, currency, 0L);
        var merchant = new Merchant(merchantId, merchantAccount);
        var entryIds = new UUID[] {
                UUID.randomUUID(),
                UUID.randomUUID()
        };
        var entries = Set.of(
                new TxEntry(new TxEntryId(entryIds[0]), paymentId, TxEntryType.DEBIT, sourceAccountId, 500L, currency),
                new TxEntry(new TxEntryId(entryIds[1]), paymentId, TxEntryType.CREDIT, merchantAccountId, 500L, currency)
        );
        var paidAt = Instant.now();

        var payment = new Payment(
                paymentId,
                sourceAccount,
                merchant,
                500L,
                currency,
                entries,
                paidAt
        );
        var result = port.savePayment(payment).block();

        assertThat(result).usingRecursiveComparison()
                .ignoringFields("merchant.account.version", "source.version")
                .isEqualTo(payment);
        assertThat(result.source().version()).isEqualTo(1L);
        assertThat(result.merchant().account().version()).isEqualTo(1L);

        var savedTxRecord = txRecordRepository.findById(paymentId.value()).block();
        var savedPaymentRecord = paymentRecordRepository.findById(paymentId.value()).block();
        var savedEntryRecords = txEntryRecordRepository.findAllById(List.of(entryIds[0], entryIds[1]))
                .collectList()
                .block();
        var savedSourceAccountRecord = accountRecordRepository.findById(sourceAccountId.value()).block();
        var savedMerchantAccountRecord = accountRecordRepository.findById(merchantAccountId.value()).block();

        assertThat(savedTxRecord).usingRecursiveComparison()
                .isEqualTo(new TxRecord(paymentId.value().toString(), paidAt));
        assertThat(savedPaymentRecord).usingRecursiveComparison()
                .isEqualTo(new PaymentRecord(paymentId.value().toString(), sourceAccountId.value(), merchantId.value(), 500L, currency.getCurrencyCode(), currency.getDefaultFractionDigits()));
        assertThat(savedEntryRecords).hasSameElementsAs(List.of(
                        new TxEntryRecord(entryIds[0].toString(), paymentId.value().toString(), "DEBIT", sourceAccountId.value(), 500L, currency.getCurrencyCode(), currency.getDefaultFractionDigits()),
                        new TxEntryRecord(entryIds[1].toString(), paymentId.value().toString(), "CREDIT", merchantAccountId.value(), 500L, currency.getCurrencyCode(), currency.getDefaultFractionDigits())
                )).hasSize(2);
        assertThat(savedSourceAccountRecord).usingRecursiveComparison()
                .ignoringFields("version")
                .isEqualTo(AccountRecord.fromAccount(sourceAccount));
        assertThat(savedSourceAccountRecord.version()).isEqualTo(1L);
        assertThat(savedMerchantAccountRecord).usingRecursiveComparison()
                .ignoringFields("version")
                .isEqualTo(AccountRecord.fromAccount(merchantAccount));
        assertThat(savedMerchantAccountRecord.version()).isEqualTo(1L);
    }

}