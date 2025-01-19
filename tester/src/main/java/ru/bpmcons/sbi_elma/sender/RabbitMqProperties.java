package ru.bpmcons.sbi_elma.sender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
@Getter
@Setter
public class RabbitMqProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    private String queue;
    private String virtualHost = "/";
    private SendMode mode = SendMode.AMQP;

    public enum SendMode {
        ADMIN_API,
        AMQP
    }
}
