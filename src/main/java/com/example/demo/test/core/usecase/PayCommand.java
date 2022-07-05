package com.example.demo.test.core.usecase;

import com.example.demo.test.core.data.AccountId;
import com.example.demo.test.core.data.MerchantId;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Currency;

public record PayCommand(
        @Valid @NotNull AccountId sourceId,
        @Valid @NotNull MerchantId merchantId,
        @Min(0) long amount,
        @NotNull Currency currency
) {
}
