package com.example.demo.test.app.adapter.controller;

import com.example.demo.test.app.adapter.controller.data.ErrorResponse;
import com.example.demo.test.app.adapter.controller.data.PaymentRequest;
import com.example.demo.test.app.adapter.controller.data.PaymentResponse;
import com.example.demo.test.core.data.*;
import com.example.demo.test.core.service.exception.InconsistentCurrencyException;
import com.example.demo.test.core.service.exception.InsufficientBalanceException;
import com.example.demo.test.core.service.exception.MerchantNotFoundException;
import com.example.demo.test.core.service.exception.SourceAccountNotFoundException;
import com.example.demo.test.core.usecase.PayCommand;
import com.example.demo.test.core.usecase.PayUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.BDDMockito.given;

@DisplayName("PaymentController")
@WebFluxTest
class PaymentControllerIT {

    @Autowired
    PaymentController controller;

    @Autowired
    ConstraintViolationControllerAdvice advice;

    @MockBean
    PayUseCase payUseCase;

    @Autowired
    WebTestClient webClient;

    @Test
    void acceptsPayments() {
        var sourceId = new AccountId(UUID.randomUUID().toString());
        var merchantId = new MerchantId(UUID.randomUUID().toString());
        var amount = 500L;
        var currency = Currency.getInstance("PHP");
        var command = new PayCommand(sourceId, merchantId, amount, currency);

        var paymentId = new PaymentId(UUID.randomUUID());
        var sourceAccount = new Account(sourceId, 200L, currency, 1L);
        var merchantAccountId = new AccountId(UUID.randomUUID().toString());
        var merchantAccount = new Account(merchantAccountId, 300L, currency, 1L);
        var merchant = new Merchant(merchantId, merchantAccount);
        var entries = Set.of(
                new TxEntry(txEntryId(), paymentId, TxEntryType.DEBIT, sourceId, amount, currency),
                new TxEntry(txEntryId(), paymentId, TxEntryType.CREDIT, merchantAccountId, amount, currency)
        );
        var payment = new Payment(paymentId, sourceAccount, merchant, amount, currency, entries, Instant.now());

        given(payUseCase.pay(command)).willReturn(Mono.just(payment));
        var request = new PaymentRequest(sourceId.value(), merchantId.value(), new BigDecimal("5.00"), currency);
        var result = webClient.post()
                .uri("/payments")
                .body(Mono.just(request), PaymentRequest.class)
                .exchange()
                .expectStatus().isEqualTo(200)
                .returnResult(PaymentResponse.class)
                .getResponseBody()
                .blockFirst();

        var expected = new PaymentResponse(
                paymentId.value(),
                sourceId.value(),
                merchantId.value(),
                new BigDecimal("5.00"),
                "PHP",
                payment.paidAt()
        );

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void returnsErrorOnSourceNotFound() {
        var sourceId = new AccountId(UUID.randomUUID().toString());
        var merchantId = new MerchantId(UUID.randomUUID().toString());
        var amount = 500L;
        var currency = Currency.getInstance("PHP");
        var command = new PayCommand(sourceId, merchantId, amount, currency);

        given(payUseCase.pay(command)).willThrow(new SourceAccountNotFoundException(sourceId));
        var request = new PaymentRequest(sourceId.value(), merchantId.value(), new BigDecimal("5.00"), currency);
        var result = webClient.post()
                .uri("/payments")
                .body(Mono.just(request), PaymentRequest.class)
                .exchange()
                .expectStatus().isEqualTo(422)
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();

        assertThat(result.code()).isEqualTo("SourceAccountNotFoundException");
        assertThat(result.message()).isEqualTo("Source account not found");
        assertThat((result.details().get("sourceId"))).isEqualTo(sourceId.value());
    }

    @Test
    void returnsErrorOnMerchantNotFound() {
        var sourceId = new AccountId(UUID.randomUUID().toString());
        var merchantId = new MerchantId(UUID.randomUUID().toString());
        var amount = 500L;
        var currency = Currency.getInstance("PHP");
        var command = new PayCommand(sourceId, merchantId, amount, currency);

        given(payUseCase.pay(command)).willThrow(new MerchantNotFoundException(merchantId));
        var request = new PaymentRequest(sourceId.value(), merchantId.value(), new BigDecimal("5.00"), currency);
        var result = webClient.post()
                .uri("/payments")
                .body(Mono.just(request), PaymentRequest.class)
                .exchange()
                .expectStatus().isEqualTo(422)
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();

        assertThat(result.code()).isEqualTo("MerchantNotFoundException");
        assertThat(result.message()).isEqualTo("Merchant not found");
        assertThat((result.details().get("merchantId"))).isEqualTo(merchantId.value());
    }

    @Test
    void returnsErrorOnInconsistentCurrency() {
        var sourceId = new AccountId(UUID.randomUUID().toString());
        var merchantId = new MerchantId(UUID.randomUUID().toString());
        var amount = 500L;
        var currency = Currency.getInstance("PHP");
        var command = new PayCommand(sourceId, merchantId, amount, currency);

        given(payUseCase.pay(command)).willThrow(new InconsistentCurrencyException(currency));
        var request = new PaymentRequest(sourceId.value(), merchantId.value(), new BigDecimal("5.00"), currency);
        var result = webClient.post()
                .uri("/payments")
                .body(Mono.just(request), PaymentRequest.class)
                .exchange()
                .expectStatus().isEqualTo(422)
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();

        assertThat(result.code()).isEqualTo("InconsistentCurrencyException");
        assertThat(result.message()).isEqualTo("Request currency does not match account or merchant account currency");
        assertThat((result.details().get("currency"))).isEqualTo(currency.getCurrencyCode());
    }

    @Test
    void returnsErrorOnInsufficientBalance() {
        var sourceId = new AccountId(UUID.randomUUID().toString());
        var merchantId = new MerchantId(UUID.randomUUID().toString());
        var amount = 5000L;
        var currency = Currency.getInstance("PHP");
        var command = new PayCommand(sourceId, merchantId, amount, currency);

        given(payUseCase.pay(command)).willThrow(new InsufficientBalanceException(currency, 2500L, 5000L));
        var request = new PaymentRequest(sourceId.value(), merchantId.value(), new BigDecimal("50.00"), currency);
        var result = webClient.post()
                .uri("/payments")
                .body(Mono.just(request), PaymentRequest.class)
                .exchange()
                .expectStatus().isEqualTo(422)
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();

        assertThat(result.code()).isEqualTo("InsufficientBalanceException");
        assertThat(result.message()).isEqualTo("Insufficient balance");
        assertThat((result.details())).isEqualTo(Map.of(
                "currency", currency.getCurrencyCode(),
                "accountBalance", new BigDecimal("25.00"),
                "amount", new BigDecimal("50.00")
        ));
    }

    @ParameterizedTest
    @MethodSource
    void returnsErrorOnInvalidRequest(PaymentRequest request, String key, String message) {
        var result = webClient.post()
                .uri("/payments")
                .body(Mono.just(request), PaymentRequest.class)
                .exchange()
                .expectStatus().isEqualTo(400)
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();

        assertThat(result.code()).isEqualTo("WebExchangeBindException");
        assertThat(result.message()).isEqualTo("Field violations found");
        assertThat(((Map<String, List>) result.details().get("violations")).get(key)).contains(message);
    }

    static Stream<Arguments> returnsErrorOnInvalidRequest() {
        var sourceId = UUID.randomUUID().toString();
        var merchantId = UUID.randomUUID().toString();
        var amount = new BigDecimal("5.00");
        var currency = Currency.getInstance("PHP");

        return Stream.of(
                of(new PaymentRequest(null, merchantId, amount, currency), "sourceId", "must not be empty"),
                of(new PaymentRequest("", merchantId, amount, currency), "sourceId", "must not be empty"),
                of(new PaymentRequest(sourceId, null, amount, currency), "merchantId", "must not be empty"),
                of(new PaymentRequest(sourceId, "", amount, currency), "merchantId", "must not be empty"),
                of(new PaymentRequest(sourceId, merchantId, null, currency), "amount", "must not be null"),
                of(new PaymentRequest(sourceId, merchantId, new BigDecimal("-1.00"), currency), "amount", "must be greater than or equal to 0"),
                of(new PaymentRequest(sourceId, merchantId, amount, null), "currency", "must not be null")
        );
    }

    TxEntryId txEntryId() {
        return new TxEntryId(UUID.randomUUID());
    }
}