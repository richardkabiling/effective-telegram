package com.example.demo.test.core.data;

import java.time.Instant;

public interface Tx {

    TxId id();
    Instant txAt();

}
