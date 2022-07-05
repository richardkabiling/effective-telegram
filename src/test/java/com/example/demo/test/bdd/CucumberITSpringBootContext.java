package com.example.demo.test.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(initializers = CucumberContextInitializer.class)
@CucumberContextConfiguration
@AutoConfigureWebTestClient
public class CucumberITSpringBootContext {

}
