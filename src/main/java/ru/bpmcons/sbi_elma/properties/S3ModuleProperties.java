package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "s3module")
@Getter
@Setter
public class S3ModuleProperties {
    private String url;
    private String port;
    private String path;
    private String apiVersion;
    private String tempSign;
    private String generatePreview;
    private String changeStorage;
    private String getMethod;
    private String putMethod;
    private String operativeBucket;
    private String archiveBucket;
    private String address;

    private Duration retryInitial = Duration.ofMillis(100);
    private Duration retryMax = Duration.ofSeconds(1);
    private double retryMultiplier = 2;
    private int retryMaxAttempts = 3;
}
