package com.example.demo.test.infra.config;

import com.example.demo.test.infra.adapter.r2dbc.AccountRepository;
import com.example.demo.test.infra.adapter.r2dbc.MerchantRepository;
import com.example.demo.test.infra.adapter.r2dbc.PaymentRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.AccountRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.PaymentRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.TxEntryRecordRepository;
import com.example.demo.test.infra.adapter.r2dbc.repository.TxRecordRepository;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@ComponentScan(basePackageClasses = {
        PaymentRepository.class,
        AccountRepository.class,
        MerchantRepository.class
})
@EnableR2dbcRepositories(basePackageClasses = {
        TxRecordRepository.class,
        PaymentRecordRepository.class,
        TxEntryRecordRepository.class,
        AccountRecordRepository.class
})
public class ReactiveRepositoryConfiguration {

}
