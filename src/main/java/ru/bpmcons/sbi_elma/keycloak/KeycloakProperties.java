package ru.bpmcons.sbi_elma.keycloak;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keycloak", ignoreInvalidFields = true)
public class KeycloakProperties {
    private String algorithm;
    private String defaultAuditory = "ecm";
    private Map<String, AppConfig> apps = new HashMap<>();

    @Deprecated
    private String pkecm;
    @Deprecated
    private String pksphere;
    @Deprecated
    private String pkinsapp;
    @Deprecated
    private String pkvirtu;
    @Deprecated
    private String pksso;

    @Data
    public static class AppConfig {
        private String publicKey;
        private String ecmAuditory;
    }
}
