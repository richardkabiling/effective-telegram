package com.example.demo.test.core.service.exception;

import com.example.demo.test.core.data.MerchantId;

public class MerchantNotFoundException extends RuntimeException {
    private final MerchantId merchantId;

    public MerchantNotFoundException(MerchantId merchantId) {
        this.merchantId = merchantId;
    }

    public MerchantId getMerchantId() {
        return merchantId;
    }
}
