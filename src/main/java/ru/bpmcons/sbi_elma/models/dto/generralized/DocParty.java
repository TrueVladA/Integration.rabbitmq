package ru.bpmcons.sbi_elma.models.dto.generralized;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.bpmcons.sbi_elma.infra.version.Since;

import java.util.Date;

@Data
public class DocParty {

    private String id_ecm_party;
    private String id_as_party;
    private String app_id;
    private String app_sysname;
    private PerecoderObject type;
    private PerecoderObject role;
//    private String type;         // Тип участника (юр. лицо, физ. лицо, ИП)
//    private String role;         // Роль участника
    private String opf;          // Код классификатора ОПФ. 5 знаков
    private String shortname;    // Краткое имя
    private String fullname;     // Полное имя
    private String fio;
    private Date birthdate;    // дата рождения
    private @JsonProperty("INN") String INN;          // ИНН - код налогоплатильщика
    private @JsonProperty("VIP") String VIP;          // тип VIP клиента
    private String identity_doc;     //Дул строкой
    private IdentityDoc identity_doc_obj;     //  ДУЛ
    @Since(major = 1, minor = 1, patch = 19)
    private String identity_doc_id;
    private String email;
}
