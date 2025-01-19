package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "methods")
@Getter
@Setter
public class MethodsName {
    private String createDoc;
    private String createIdentityDoc;
    private String searchDoc;
    private String getDoc;
    private String getIdentityDoc;
    private String updateDoc;
    private String updateIdentityDoc;
    private String deleteDoc;
    private String deleteFile;
    private String generatePreview;
    private String notificationUpload;
}
