package com.example.demo.test.infra.adapter.r2dbc;

import com.example.demo.test.core.data.Merchant;
import com.example.demo.test.core.data.MerchantId;
import com.example.demo.test.core.port.RetrieveMerchantPort;
import com.example.demo.test.infra.adapter.r2dbc.repository.AccountRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.MerchantRecordRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class MerchantRepository implements RetrieveMerchantPort {

    private final MerchantRecordRepository merchantRecordRepository;
    private final AccountRecordRepository accountRecordRepository;

    public MerchantRepository(MerchantRecordRepository merchantRecordRepository, AccountRecordRepository accountRecordRepository) {
        this.merchantRecordRepository = merchantRecordRepository;
        this.accountRecordRepository = accountRecordRepository;
    }

    @Override
    public Mono<Merchant> findMerchant(MerchantId id) {
        return merchantRecordRepository.findById(id.value())
                .flatMap(merchantRecord -> accountRecordRepository.findById(merchantRecord.accountId())
                        .map(accountRecord -> new Merchant(
                                new MerchantId(merchantRecord.id()),
                                accountRecord.toAccount()
                        )));
    }
}
