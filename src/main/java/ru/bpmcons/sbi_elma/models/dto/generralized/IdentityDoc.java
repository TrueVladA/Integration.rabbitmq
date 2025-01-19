package ru.bpmcons.sbi_elma.models.dto.generralized;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.models.dto.jwt.JwtToken;
import ru.bpmcons.sbi_elma.models.dto.responseMq.CodeMessage;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class IdentityDoc extends CodeMessage {
    private String rquid;
    private JwtToken jwt_token;
    private String id_ecm_doc;
    private String id_as_doc;
    private String app_id;
    private String app_sysname;
    private PerecoderObject code_identitydoc;
//    private String code_identitydoc;                    // Наименование типа из классификатора документов ДУЛ
    private String type_identitydoc;       //Название документа ДУЛ
    private String series;       // Серия ДУЛ
    private String number;       // Номер ДУЛ
    private String full_number;
    private @JsonProperty("issue_date") Date issue_date;     // Дата выдачи ДУЛ. UTC+3
    private Date end_date;
    private String fio;
    private String issued;
    private FileMetadata[] file_metadata;
}
