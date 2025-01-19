package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "behavior")
@Getter
@Setter
public class BehaviorProperties {
    /**
     * Убрать архивные документы из SearchDoc, проверок Create/Update, etc
     */
    private boolean hideArchivedDocs = false;
}
