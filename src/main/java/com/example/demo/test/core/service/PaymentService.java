package com.example.demo.test.core.service;

import com.example.demo.test.core.data.*;
import com.example.demo.test.core.port.RetrieveAccountPort;
import com.example.demo.test.core.port.RetrieveMerchantPort;
import com.example.demo.test.core.port.SavePaymentPort;
import com.example.demo.test.core.service.exception.InconsistentCurrencyException;
import com.example.demo.test.core.service.exception.InsufficientBalanceException;
import com.example.demo.test.core.service.exception.MerchantNotFoundException;
import com.example.demo.test.core.service.exception.SourceAccountNotFoundException;
import com.example.demo.test.core.usecase.PayCommand;
import com.example.demo.test.core.usecase.PayUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Service
public class PaymentService implements PayUseCase {

    private final PaymentIdGenerationStrategy paymentIdGenerationStrategy;
    private final TxEntryIdGenerationStrategy txEntryIdGenerationStrategy;
    private final Supplier<Clock> clockSupplier;
    private final RetrieveMerchantPort retrieveMerchantPort;
    private final RetrieveAccountPort retrieveAccountPort;
    private final SavePaymentPort savePaymentPort;

    public PaymentService(
            PaymentIdGenerationStrategy paymentIdGenerationStrategy,
            TxEntryIdGenerationStrategy txEntryIdGenerationStrategy,
            Supplier<Clock> clockSupplier,
            RetrieveMerchantPort retrieveMerchantPort,
            RetrieveAccountPort retrieveAccountPort,
            SavePaymentPort savePaymentPort
    ) {
        this.paymentIdGenerationStrategy = paymentIdGenerationStrategy;
        this.txEntryIdGenerationStrategy = txEntryIdGenerationStrategy;
        this.clockSupplier = clockSupplier;
        this.retrieveMerchantPort = retrieveMerchantPort;
        this.retrieveAccountPort = retrieveAccountPort;
        this.savePaymentPort = savePaymentPort;
    }

    @Override
    public Mono<Payment> pay(PayCommand command) {
        var sourceAccount = retrieveAccountPort.findAccount(command.sourceId())
                .switchIfEmpty(Mono.error(() -> new SourceAccountNotFoundException(command.sourceId())));
        var merchant = retrieveMerchantPort.findMerchant(command.merchantId())
                .switchIfEmpty(Mono.error(() -> new MerchantNotFoundException(command.merchantId())));
        var amount = command.amount();
        var currency = command.currency();

        return Mono.zip(sourceAccount, merchant, (sa, m) -> toPayment(amount, currency, sa, m))
                .flatMap(savePaymentPort::savePayment);
    }

    private Payment toPayment(long amount, Currency currency, Account sourceAccount, Merchant merchant) {
        var merchantAccount = merchant.account();

        if (isInconsistent(sourceAccount, currency) || isInconsistent(merchantAccount, currency)) {
            throw new InconsistentCurrencyException(currency);
        }

        if (sourceAccount.amount() < amount) {
            throw new InsufficientBalanceException(currency, sourceAccount.amount(), amount);
        }

        var paymentId = paymentIdGenerationStrategy.get();
        var paidAt = Instant.now(clockSupplier.get());
        var updatedSourceAccount = new Account(
                sourceAccount.id(),
                sourceAccount.amount() - amount,
                sourceAccount.currency(),
                sourceAccount.version()
        );
        var updatedMerchantAccount = new Account(
                merchantAccount.id(),
                merchantAccount.amount() + amount,
                merchantAccount.currency(),
                merchantAccount.version()
        );
        var updatedMerchant = new Merchant(
                merchant.id(),
                updatedMerchantAccount
        );
        var entries = Set.of(
                new TxEntry(
                        txEntryIdGenerationStrategy.get(),
                        paymentId,
                        TxEntryType.DEBIT,
                        sourceAccount.id(),
                        amount,
                        currency
                ),
                new TxEntry(
                        txEntryIdGenerationStrategy.get(),
                        paymentId,
                        TxEntryType.CREDIT,
                        merchantAccount.id(),
                        amount,
                        currency
                )
        );

        return new Payment(
                paymentId,
                updatedSourceAccount,
                updatedMerchant,
                amount,
                currency,
                entries,
                paidAt
        );
    }

    private boolean isInconsistent(Account sourceAccount, Currency currency) {
        return !Objects.equals(currency, sourceAccount.currency());
    }
}
