package ru.bpmcons.sbi_elma.models.dto.generralized;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * Не актуален с версии 1.1.13
 */
@Data
public class Recipient {
    private String id_ecm_recipient;
    private String id_as_recipient;
    private String app_id;
    private String type;
    private String role;
    private String shortname;
    private String fullname;
    private String fio;
    private @JsonProperty("birthdate") Date birthdate;
    private @JsonProperty("INN") String INN;
    private @JsonProperty("VIP") String VIP;
    private String identity_doc;
    private IdentityDoc id_ecm_identity_doc;
}
