package com.example.demo.test.core.service;

import com.example.demo.test.core.data.TxEntryId;

import java.util.function.Supplier;

public interface TxEntryIdGenerationStrategy extends Supplier<TxEntryId> {
}
