package com.example.demo.test;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

public class TestEnvironment {

    private static DockerComposeContainer container;

    public static void start() {
        if(container == null) {
            container = new DockerComposeContainer(new File("docker-compose.yml"))
                    .withExposedService("postgres", 5432, Wait.forListeningPort())
                    .withLocalCompose(true);
            container.start();
        }
    }
}
