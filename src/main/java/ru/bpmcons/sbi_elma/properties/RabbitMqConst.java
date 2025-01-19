package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
@Getter
@Setter
public class RabbitMqConst {
    private String host;
    private int port;
    private String username;
    private String usernameTest;
    private String password;
    private String passwordTest;
    private String virtualhost;
    private String virtualhostTest;
    private String responseExchange;
    private String requestQueue;
    private int prefetchCount;
    private Duration retryTimeout = Duration.ofSeconds(3);
    private int parallelism = 8;
    private int workerParallelism = parallelism * 512;
    private Duration throttlerQueueRecheckTimeout = Duration.ofMillis(50);
}
