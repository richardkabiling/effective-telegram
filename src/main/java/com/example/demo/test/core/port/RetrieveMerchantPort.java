package com.example.demo.test.core.port;

import com.example.demo.test.core.data.Merchant;
import com.example.demo.test.core.data.MerchantId;
import reactor.core.publisher.Mono;

public interface RetrieveMerchantPort {

    Mono<Merchant> findMerchant(MerchantId id);

}
