package ru.bpmcons.sbi_elma.models.dto.generralized;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Не актуален с версии 1.1.13
 */
@Data
public class Sender {
    private String id_ecm_sender;
    private String id_as_sender;
    private String app_id;
    private String type;             // Тип участника
    private String role;             // Роль участника
    private String shortname;        // Краткое имя
    private String fullname;         // Полное имя
    private String fio;
    private String birthdate;        // Дата рождения
    private @JsonProperty("INN") String INN;              // ИНН
    private @JsonProperty("VIP") String VIP;              // Признак VIP
    private String identity_doc;
    private IdentityDoc id_ecm_identity_doc;    // ДУЛ
}
