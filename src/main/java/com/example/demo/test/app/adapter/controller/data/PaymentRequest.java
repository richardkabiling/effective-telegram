package com.example.demo.test.app.adapter.controller.data;

import com.example.demo.test.app.adapter.controller.Amounts;
import com.example.demo.test.core.data.AccountId;
import com.example.demo.test.core.data.MerchantId;
import com.example.demo.test.core.usecase.PayCommand;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

import static com.example.demo.test.app.adapter.controller.Amounts.*;

public record PaymentRequest(
        @NotEmpty String sourceId,
        @NotEmpty String merchantId,
        @NotNull @DecimalMin(value = "0") BigDecimal amount,
        @NotNull Currency currency
) {

    public PayCommand toPayCommand() {
        return new PayCommand(
                new AccountId(sourceId),
                new MerchantId(merchantId),
                toAdjustedAmount(currency, amount),
                currency
        );
    }

}
