package com.example.demo.test.core.usecase;

import com.example.demo.test.core.data.Payment;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
public interface PayUseCase {

    Mono<Payment> pay(@NotNull @Valid PayCommand command);

}
