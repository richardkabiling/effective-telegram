package com.example.demo.test.app.adapter.controller;

import com.example.demo.test.app.adapter.controller.data.ErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestControllerAdvice
public class ConstraintViolationControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ErrorResponse> handle(WebExchangeBindException e) {
        return Mono.just(
                new ErrorResponse(
                    e.getClass().getSimpleName(),
                    "Field violations found",
                    Map.of("violations", e.getFieldErrors()
                            .stream()
                            .collect(groupingBy(FieldError::getField, mapping(DefaultMessageSourceResolvable::getDefaultMessage, toList()))))
        )
        );
    }

}
