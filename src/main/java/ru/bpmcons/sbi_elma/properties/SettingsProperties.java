package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "settings")
public class SettingsProperties {
    private PermissionValidation permissionValidation;

    public enum PermissionValidation {
        NONE,
        PARTIAL,
        FULL
    }
}
