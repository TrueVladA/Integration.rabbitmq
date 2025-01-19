package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "message-header")
@Getter
@Setter
public class MessageHeaderNames {
    private String versionApi;
    private String method;
    private String responseCode;
    private String responseMessage;
    private String content;
    private String requestStart;
    private String requestFinish;
    private String features = "features";
}
