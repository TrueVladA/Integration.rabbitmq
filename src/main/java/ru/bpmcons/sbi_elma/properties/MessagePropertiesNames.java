package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "message-properties")
@Getter
@Setter
public class MessagePropertiesNames {
    private String exchange;
    private String routingKey;
    private String deliveryMode;
    private String messageId;
    private String contentType;
    private String type;
    private String appId;
    private String timestamp;
    private String replyTo;
    private String correlationId;
}
