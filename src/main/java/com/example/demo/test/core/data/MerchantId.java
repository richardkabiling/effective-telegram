package com.example.demo.test.core.data;

import javax.validation.constraints.NotEmpty;

public record MerchantId(@NotEmpty String value) {
}
