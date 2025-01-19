package ru.bpmcons.sbi_elma.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sys-names")
@Getter
@Setter
public class SysNamesConstants {
    private String task;
    private String damage;
    private String fileMetadata;
    private String dul;
    private String contract;
    private String agreement;
    private String complaint;
    private String dulTypes;
    private String docTypes;
    private String creatorEditor;
    private String commonSystem;
    private String partyType;
    private String partyRole;
    private String productLine;
    private String contractType;
    private String extract;
    private String resolution;
    private String reference;
    private String other;
    private String lawsuit;
    private String orgleg;
    private String vip;
    private String subrogarion;
    private String extension;
    private String block;
    private String letter;
    private String products;
    private String damageOsage;
    private String finDoc = "fin_doc";
    private String fileProjects = "file_projects";
    private String roles = "access_roles";
}
