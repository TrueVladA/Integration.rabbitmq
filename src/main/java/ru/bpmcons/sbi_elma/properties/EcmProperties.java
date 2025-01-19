package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "ecm")
@Getter
@Setter
public class EcmProperties {
    private String urlApp;
    private String versionApi;
    private String pathToDocuments;
    private String pathToReferences;
    private String bearerAuth;
    private String pathToDisk;
    private Duration connectTimeout = Duration.ofMinutes(3);
    private Duration readTimeout = Duration.ofMinutes(3);
    private int errorRetries = 2;
    private Duration errorRetryTimeout = Duration.ofMillis(10);
}
