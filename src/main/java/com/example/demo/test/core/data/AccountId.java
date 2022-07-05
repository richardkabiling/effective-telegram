package com.example.demo.test.core.data;

import javax.validation.constraints.NotEmpty;

public record AccountId(@NotEmpty String value) {
}
