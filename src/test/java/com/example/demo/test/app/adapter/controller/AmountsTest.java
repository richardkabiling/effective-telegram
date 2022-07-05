package com.example.demo.test.app.adapter.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Amounts")
class AmountsTest {

    @ParameterizedTest
    @MethodSource("source")
    void areConvertedFromBigDecimalToLong(Currency currency, long amountInLong, BigDecimal amountInBigDecimal) {
        assertThat(Amounts.fromAdjustedAmount(currency, amountInLong)).isEqualTo(amountInBigDecimal);
    }

    @ParameterizedTest
    @MethodSource("source")
    void areConvertedFromLongToBigDecimal(Currency currency, long amountInLong, BigDecimal amountInBigDecimal) {
        assertThat(Amounts.toAdjustedAmount(currency, amountInBigDecimal)).isEqualTo(amountInLong);
    }

    public static Stream<Arguments> source() {
        return Stream.of(
                of(Currency.getInstance("PHP"), 4500, new BigDecimal("45.00")),
                of(Currency.getInstance("JPY"), 45, new BigDecimal("45")),
                of(Currency.getInstance("JOD"), 45000, new BigDecimal("45.000")),
                of(Currency.getInstance("PHP"), 500, new BigDecimal("5.00")),
                of(Currency.getInstance("JPY"), 500, new BigDecimal("500")),
                of(Currency.getInstance("JOD"), 500, new BigDecimal("0.500"))
        );

    }

}