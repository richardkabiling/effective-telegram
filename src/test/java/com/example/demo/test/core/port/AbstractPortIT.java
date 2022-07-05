package com.example.demo.test.core.port;

import com.example.demo.test.TestEnvironment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractPortIT {

    static {
        TestEnvironment.start();
    }
}
