package com.example.demo.test.app.adapter.controller;

import com.example.demo.test.app.adapter.controller.data.ErrorResponse;
import com.example.demo.test.app.adapter.controller.data.PaymentRequest;
import com.example.demo.test.app.adapter.controller.data.PaymentResponse;
import com.example.demo.test.core.service.exception.InconsistentCurrencyException;
import com.example.demo.test.core.service.exception.InsufficientBalanceException;
import com.example.demo.test.core.service.exception.MerchantNotFoundException;
import com.example.demo.test.core.service.exception.SourceAccountNotFoundException;
import com.example.demo.test.core.usecase.PayUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;

import static com.example.demo.test.app.adapter.controller.Amounts.fromAdjustedAmount;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PayUseCase payUseCase;

    public PaymentController(PayUseCase payUseCase) {
        this.payUseCase = payUseCase;
    }

    @PostMapping
    public Mono<PaymentResponse> paymentResponse(@Valid @RequestBody PaymentRequest request) {
        return payUseCase.pay(request.toPayCommand())
                .map(PaymentResponse::fromPayment);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(SourceAccountNotFoundException.class)
    public Mono<ErrorResponse> handle(SourceAccountNotFoundException e) {
        return Mono.just(
                new ErrorResponse(
                        e.getClass().getSimpleName(),
                        "Source account not found",
                        Map.of("sourceId", e.getSourceId().value()))
        );
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MerchantNotFoundException.class)
    public Mono<ErrorResponse> handle(MerchantNotFoundException e) {
        return Mono.just(
                new ErrorResponse(
                        e.getClass().getSimpleName(),
                        "Merchant not found",
                        Map.of("merchantId", e.getMerchantId().value()))
        );
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InconsistentCurrencyException.class)
    public Mono<ErrorResponse> handle(InconsistentCurrencyException e) {
        return Mono.just(
                new ErrorResponse(
                        e.getClass().getSimpleName(),
                        "Request currency does not match account or merchant account currency",
                        Map.of("currency", e.getCurrency()))
        );
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ErrorResponse> handle(InsufficientBalanceException e) {
        return Mono.just(
                new ErrorResponse(
                        e.getClass().getSimpleName(),
                        "Insufficient balance",
                        Map.of(
                                "currency", e.getCurrency(),
                                "accountBalance", fromAdjustedAmount(e.getCurrency(), e.getAccountBalance()),
                                "amount", fromAdjustedAmount(e.getCurrency(), e.getAmount())
                        ))
        );
    }

}
