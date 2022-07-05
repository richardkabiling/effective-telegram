package com.example.demo.test.core.service.exception;

import com.example.demo.test.core.data.AccountId;

public class SourceAccountNotFoundException extends RuntimeException {
    private final AccountId sourceId;

    public SourceAccountNotFoundException(AccountId sourceId) {
        this.sourceId = sourceId;
    }

    public AccountId getSourceId() {
        return sourceId;
    }
}
