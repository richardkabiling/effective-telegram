package com.example.demo.test.infra.adapter.r2dbc;

import com.example.demo.test.core.data.*;
import com.example.demo.test.core.port.SavePaymentPort;
import com.example.demo.test.infra.adapter.r2dbc.record.AccountRecord;
import com.example.demo.test.infra.adapter.r2dbc.record.PaymentRecord;
import com.example.demo.test.infra.adapter.r2dbc.record.TxEntryRecord;
import com.example.demo.test.infra.adapter.r2dbc.record.TxRecord;
import com.example.demo.test.infra.adapter.r2dbc.repository.AccountRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.PaymentRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.TxEntryRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.TxRecordRepository;
import org.javatuples.Quintet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Currency;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PaymentRepository implements SavePaymentPort {

    private final TxRecordRepository txRecordRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final TxEntryRecordRepository txEntryRecordRepository;
    private final AccountRecordRepository accountRecordRepository;

    public PaymentRepository(
            TxRecordRepository txRecordRepository,
            PaymentRecordRepository paymentRecordRepository,
            TxEntryRecordRepository txEntryRecordRepository,
            AccountRecordRepository accountRecordRepository
    ) {
        this.txRecordRepository = txRecordRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.txEntryRecordRepository = txEntryRecordRepository;
        this.accountRecordRepository = accountRecordRepository;
    }

    @Transactional
    @Override
    public Mono<Payment> savePayment(Payment payment) {
        var txRecord = TxRecord.fromTx(payment);
        var paymentRecord = PaymentRecord.fromPayment(payment);
        var paymentEntries = fromPaymentEntries(payment.entries());

        var sourceAccount = payment.source();
        var merchantAccount = payment.merchant().account();
        var sourceAccountRecord = AccountRecord.fromAccount(sourceAccount);
        var merchantAccountRecord = AccountRecord.fromAccount(merchantAccount);

        var savedTx = txRecordRepository.save(txRecord).cache();
        var savedPayment = savedTx.then(paymentRecordRepository.save(paymentRecord));
        var savedEntries = savedTx.thenMany(txEntryRecordRepository.saveAll(paymentEntries))
                .collect(Collectors.toSet());
        var savedSourceAccount = accountRecordRepository.save(sourceAccountRecord);
        var savedMerchantAccount = accountRecordRepository.save(merchantAccountRecord);

        return Mono.zip(savedTx, savedPayment, savedEntries, savedSourceAccount, savedMerchantAccount)
                .map(rs -> new Quintet<>(rs.getT1(), rs.getT2(), rs.getT3(), rs.getT4(), rs.getT5()))
                .map(rs -> new Payment(
                        new PaymentId(UUID.fromString(rs.getValue1().id())),
                        toAccount(rs.getValue3()),
                        new Merchant(payment.merchant().id(), toAccount(rs.getValue4())),
                        rs.getValue1().amount(),
                        Currency.getInstance(rs.getValue1().currency()),
                        rs.getValue2()
                                .stream()
                                .map(this::toPaymentEntry)
                                .collect(Collectors.toSet()),
                        rs.getValue0().txAt()
                ));
    }

    private Account toAccount(AccountRecord record) {
        return new Account(
                new AccountId(record.id()),
                record.amount(),
                Currency.getInstance(record.currency()),
                record.version()
        );
    }

    private TxEntry toPaymentEntry(TxEntryRecord record) {
        return new TxEntry(
                new TxEntryId(UUID.fromString(record.id())),
                new PaymentId(UUID.fromString(record.txId())),
                TxEntryType.valueOf(record.type()),
                new AccountId(record.accountId()),
                record.amount(),
                Currency.getInstance(record.currency())
        );
    }

    private Set<TxEntryRecord> fromPaymentEntries(Set<TxEntry> entries) {
        return entries.stream()
                .map(TxEntryRecord::fromTxEntry)
                .collect(Collectors.toSet());
    }

}
