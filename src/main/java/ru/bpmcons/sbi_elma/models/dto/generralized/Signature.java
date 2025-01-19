package ru.bpmcons.sbi_elma.models.dto.generralized;

import lombok.Data;
import ru.bpmcons.sbi_elma.utils.HideFromLogs;

import java.util.Date;

@Data
public class Signature {

    private String id_ecm_sig;
    private String id_as_sig;
    private String app_id;
    private String signature_type;       // Тип подписи
    private String name_sig;             // Наименование файла подписи
    private Date datetime_sig;           // Дата-время подписи
    private String hash;                 // Хэш подписи
    private String signatory_info;       // Информация о подписанте
    private String url_as;
    @HideFromLogs
    private String url_file;
}
