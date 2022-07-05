package com.example.demo.test.core.usecase;

import com.example.demo.test.core.data.*;
import com.example.demo.test.core.port.RetrieveAccountPort;
import com.example.demo.test.core.port.RetrieveMerchantPort;
import com.example.demo.test.core.port.SavePaymentPort;
import com.example.demo.test.core.service.PaymentIdGenerationStrategy;
import com.example.demo.test.core.service.PaymentService;
import com.example.demo.test.core.service.TxEntryIdGenerationStrategy;
import com.example.demo.test.core.service.exception.InconsistentCurrencyException;
import com.example.demo.test.core.service.exception.InsufficientBalanceException;
import com.example.demo.test.core.service.exception.MerchantNotFoundException;
import com.example.demo.test.core.service.exception.SourceAccountNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.when;

@DisplayName("PayUseCase")
@SpringBootTest(classes = {
        PaymentService.class,
        ValidationAutoConfiguration.class
})
class PayUseCaseTest {

    @Autowired
    PayUseCase payUseCase;

    @MockBean
    PaymentIdGenerationStrategy paymentIdGenerationStrategy;

    @MockBean
    TxEntryIdGenerationStrategy txEntryIdGenerationStrategy;

    @MockBean
    Supplier<Clock> clockSupplier;

    @MockBean
    RetrieveAccountPort retrieveAccountPort;

    @MockBean
    RetrieveMerchantPort retrieveMerchantPort;

    @MockBean
    SavePaymentPort savePaymentPort;

    @Test
    void pays() {
        var paymentId = new PaymentId(UUID.randomUUID());
        when(paymentIdGenerationStrategy.get()).thenReturn(paymentId);

        var txEntryIds = new TxEntryId[] {
                new TxEntryId(UUID.randomUUID()),
                new TxEntryId(UUID.randomUUID())
        };
        when(txEntryIdGenerationStrategy.get()).thenReturn(txEntryIds[0], txEntryIds[1]);

        var now = Instant.now();
        when(clockSupplier.get()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));

        var currency = Currency.getInstance("PHP");

        var account = account(currency, 2000L);
        when(retrieveAccountPort.findAccount(account.id())).thenReturn(Mono.just(account));

        var merchant = merchant(currency, 200L);
        when(retrieveMerchantPort.findMerchant(merchant.id())).thenReturn(Mono.just(merchant));

        var payment = new Payment(
                paymentId,
                new Account(account.id(), 1500L, currency, 0L),
                new Merchant(merchant.id(), new Account(merchant.account().id(), 700L, currency, 0L)),
                500L,
                currency,
                Set.of(
                        new TxEntry(txEntryIds[0], paymentId, TxEntryType.DEBIT, account.id(), 500L, currency),
                        new TxEntry(txEntryIds[1], paymentId, TxEntryType.CREDIT, merchant.account().id(), 500L, currency)
                ),
                now
        );
        when(savePaymentPort.savePayment(payment)).thenReturn(Mono.just(payment));

        var command = new PayCommand(
                account.id(),
                merchant.id(),
                500,
                currency
        );
        var result = payUseCase.pay(command).block();

        assertThat(result).isEqualTo(payment);
    }

    @Test
    void throwsWhenAccountNotFound() {
        var currency = Currency.getInstance("PHP");

        var accountId = new AccountId(UUID.randomUUID().toString());
        when(retrieveAccountPort.findAccount(accountId)).thenReturn(Mono.empty());

        var merchant = merchant(currency, 200L);
        when(retrieveMerchantPort.findMerchant(merchant.id())).thenReturn(Mono.just(merchant));

        var command = new PayCommand(
                accountId,
                merchant.id(),
                500,
                currency
        );
        var throwable = catchThrowableOfType(() -> payUseCase.pay(command).block(), SourceAccountNotFoundException.class);

        assertThat(throwable.getSourceId()).isEqualTo(accountId);
    }

    @Test
    void throwsWhenMerchantNotFound() {
        var currency = Currency.getInstance("PHP");

        var account = account(currency, 2000L);
        when(retrieveAccountPort.findAccount(account.id())).thenReturn(Mono.just(account));

        var merchantId = new MerchantId(UUID.randomUUID().toString());
        when(retrieveMerchantPort.findMerchant(merchantId)).thenReturn(Mono.empty());

        var command = new PayCommand(
                account.id(),
                merchantId,
                500,
                currency
        );
        var throwable = catchThrowableOfType(() -> payUseCase.pay(command).block(), MerchantNotFoundException.class);

        assertThat(throwable.getMerchantId()).isEqualTo(merchantId);
    }

    @Test
    void throwsWhenCurrencyInconsistentToAccount() {
        var paymentId = new PaymentId(UUID.randomUUID());
        when(paymentIdGenerationStrategy.get()).thenReturn(paymentId);

        var txEntryIds = new TxEntryId[] {
                new TxEntryId(UUID.randomUUID()),
                new TxEntryId(UUID.randomUUID())
        };
        when(txEntryIdGenerationStrategy.get()).thenReturn(txEntryIds[0], txEntryIds[1]);

        var now = Instant.now();
        when(clockSupplier.get()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));

        var currency = Currency.getInstance("PHP");

        var account = account(Currency.getInstance("EUR"), 2000L);
        when(retrieveAccountPort.findAccount(account.id())).thenReturn(Mono.just(account));

        var merchant = merchant(currency, 200L);
        when(retrieveMerchantPort.findMerchant(merchant.id())).thenReturn(Mono.just(merchant));

        var command = new PayCommand(
                account.id(),
                merchant.id(),
                500,
                currency
        );
        var throwable = catchThrowableOfType(() -> payUseCase.pay(command).block(), InconsistentCurrencyException.class);

        assertThat(throwable.getCurrency()).isEqualTo(command.currency());
    }

    @Test
    void throwsWhenCurrencyInconsistentToMerchant() {
        var paymentId = new PaymentId(UUID.randomUUID());
        when(paymentIdGenerationStrategy.get()).thenReturn(paymentId);

        var txEntryIds = new TxEntryId[] {
                new TxEntryId(UUID.randomUUID()),
                new TxEntryId(UUID.randomUUID())
        };
        when(txEntryIdGenerationStrategy.get()).thenReturn(txEntryIds[0], txEntryIds[1]);

        var now = Instant.now();
        when(clockSupplier.get()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));

        var currency = Currency.getInstance("PHP");

        var account = account(currency, 2000L);
        when(retrieveAccountPort.findAccount(account.id())).thenReturn(Mono.just(account));

        var merchant = merchant(Currency.getInstance("EUR"), 200L);
        when(retrieveMerchantPort.findMerchant(merchant.id())).thenReturn(Mono.just(merchant));

        var command = new PayCommand(
                account.id(),
                merchant.id(),
                500,
                currency
        );
        var throwable = catchThrowableOfType(() -> payUseCase.pay(command).block(), InconsistentCurrencyException.class);

        assertThat(throwable.getCurrency()).isEqualTo(command.currency());
    }

    @Test
    void throwsWhenInsufficientBalance() {
        var paymentId = new PaymentId(UUID.randomUUID());
        when(paymentIdGenerationStrategy.get()).thenReturn(paymentId);

        var txEntryIds = new TxEntryId[] {
                new TxEntryId(UUID.randomUUID()),
                new TxEntryId(UUID.randomUUID())
        };
        when(txEntryIdGenerationStrategy.get()).thenReturn(txEntryIds[0], txEntryIds[1]);

        var now = Instant.now();
        when(clockSupplier.get()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));

        var currency = Currency.getInstance("PHP");

        var account = account(currency, 400L);
        when(retrieveAccountPort.findAccount(account.id())).thenReturn(Mono.just(account));

        var merchant = merchant(Currency.getInstance("PHP"), 200L);
        when(retrieveMerchantPort.findMerchant(merchant.id())).thenReturn(Mono.just(merchant));

        var command = new PayCommand(
                account.id(),
                merchant.id(),
                500L,
                currency
        );
        var throwable = catchThrowableOfType(() -> payUseCase.pay(command).block(), InsufficientBalanceException.class);

        assertThat(throwable.getAmount()).isEqualTo(500L);
        assertThat(throwable.getAccountBalance()).isEqualTo(400L);
        assertThat(throwable.getCurrency()).isEqualTo(command.currency());
    }

    @ParameterizedTest
    @MethodSource
    void throwsWhenInvalidParameters(PayCommand command, String messageSegment) {
        var throwable = catchThrowableOfType(() -> payUseCase.pay(command), ConstraintViolationException.class);

        assertThat(throwable).hasMessageContaining(messageSegment);
    }

    static Stream<Arguments> throwsWhenInvalidParameters() {
        var sourceId = new AccountId(UUID.randomUUID().toString());
        var merchantId = new MerchantId(UUID.randomUUID().toString());
        var amount = 0;
        var currency = Currency.getInstance("PHP");

        return Stream.of(
                of(null, "pay.command: must not be null"),
                of(new PayCommand(null, merchantId, amount, currency), "pay.command.sourceId: must not be null"),
                of(new PayCommand(sourceId, null, amount, currency), "pay.command.merchantId: must not be null"),
                of(new PayCommand(sourceId, merchantId, -1L, currency), "pay.command.amount: must be greater than or equal to 0"),
                of(new PayCommand(sourceId, merchantId, amount, null), "pay.command.currency: must not be null"),
                of(new PayCommand(new AccountId(null), merchantId, amount, currency), "pay.command.sourceId.value: must not be empty"),
                of(new PayCommand(new AccountId(""), merchantId, amount, null), "pay.command.sourceId.value: must not be empty"),
                of(new PayCommand(sourceId, new MerchantId(null), amount, null), "pay.command.merchantId.value: must not be empty"),
                of(new PayCommand(sourceId, new MerchantId(""), amount, null), "pay.command.merchantId.value: must not be empty")
        );
    }

    Merchant merchant(Currency currency, long amount) {
        var id = new MerchantId(UUID.randomUUID().toString());
        return new Merchant(id, account(currency, amount));
    }

    Account account(Currency currency, long amount) {
        var id = new AccountId(UUID.randomUUID().toString());
        return account(id, currency, amount);
    }

    Account account(AccountId id, Currency currency, long amount) {
        return new Account(id, amount, currency, 0L);
    }
}