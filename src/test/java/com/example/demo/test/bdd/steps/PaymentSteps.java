package com.example.demo.test.bdd.steps;

import com.example.demo.test.app.adapter.controller.data.ErrorResponse;
import com.example.demo.test.app.adapter.controller.data.PaymentRequest;
import com.example.demo.test.app.adapter.controller.data.PaymentResponse;
import com.example.demo.test.infra.adapter.r2dbc.repository.PaymentRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.TxEntryRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.TxRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

import static com.example.demo.test.app.adapter.controller.Amounts.toAdjustedAmount;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentSteps {

    @Autowired
    WebTestClient webClient;

    @Autowired
    PaymentRecordRepository paymentRecordRepository;

    @Autowired
    TxRecordRepository txRecordRepository;

    @Autowired
    TxEntryRecordRepository txEntryRecordRepository;

    @Autowired
    ObjectMapper mapper;

    FluxExchangeResult<Map<String, Object>> response;

    Map<String, Object> body;

    @After(order = 30)
    public void tearDown() {
        txEntryRecordRepository.deleteAll().block();
        paymentRecordRepository.deleteAll().block();
        txRecordRepository.deleteAll().block();
    }

    @When("the client pays {word} {bigdecimal} using account {string} to merchant {string}")
    public void theClientPaysUsingAccountToMerchant(
            String currency,
            BigDecimal amount,
            String accountId,
            String merchantId
    ) {
        var request = new PaymentRequest(accountId, merchantId, amount, Currency.getInstance(currency));
        response = webClient.post()
                .uri("/payments")
                .body(Mono.just(request), PaymentRequest.class)
                .exchange()
                .returnResult(new ParameterizedTypeReference<>() {
                });
        body = response.getResponseBody().blockFirst();

    }

    @Then("the payment is accepted")
    public void thePaymentIsAccepted() {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @Then("the payment is unprocessable")
    public void thePaymentIsUnprocessable() {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Then("the payment is saved")
    public void thePaymentIsSaved() {
        var id = mapper.convertValue(body, PaymentResponse.class).id();

        var savedPayment = paymentRecordRepository.findById(id)
                .blockOptional();
        var savedTx = txRecordRepository.findById(id)
                .blockOptional();
        var savedTxEntries = txEntryRecordRepository.findByTxId(id)
                .collectList()
                .blockOptional();

        assertThat(savedPayment).isNotEmpty();
        assertThat(savedTx).isNotEmpty();
        assertThat(savedTxEntries).isNotEmpty();
    }

    @Then("the payment has {int} transaction entries")
    public void thePaymentHasANumberOfTransactionEntries(Integer count) {
        var id = mapper.convertValue(body, PaymentResponse.class).id();

        var savedTxEntries = txEntryRecordRepository.findByTxId(id)
                .collectList()
                .block();

        assertThat(savedTxEntries).hasSize(count);
    }

    @Then("the payment has a {word} transaction entry of {word} {bigdecimal} on account {string}")
    public void the_payment_has_a_debit_transaction_entry_of_php_on_account_a(
            String entryType,
            String currencyAsString,
            BigDecimal amount,
            String accountId
    ) {
        var id = mapper.convertValue(body, PaymentResponse.class).id();
        var currency = Currency.getInstance(currencyAsString);

        var savedTxEntries = txEntryRecordRepository.findByTxId(id)
                .collectList()
                .block();

        assertThat(savedTxEntries).anySatisfy(record -> {
            assertThat(record.accountId()).isEqualTo(accountId);
            assertThat(record.type()).isEqualTo(entryType);
            assertThat(record.amount()).isEqualTo(toAdjustedAmount(currency, amount));
        });
    }

    @Then("the payment error code is {string}")
    public void thePaymentErrorCodeIs(String code) {
        assertThat(mapper.convertValue(body, ErrorResponse.class).code())
                .isEqualTo(code);
    }

    @And("the payment error message is {string}")
    public void thePaymentErrorMessageIs(String message) {
        assertThat(mapper.convertValue(body, ErrorResponse.class).message())
                .isEqualTo(message);
    }
}
