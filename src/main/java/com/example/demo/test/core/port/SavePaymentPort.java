package com.example.demo.test.core.port;

import com.example.demo.test.core.data.Payment;
import reactor.core.publisher.Mono;

public interface SavePaymentPort {

    Mono<Payment> savePayment(Payment payment);

}
