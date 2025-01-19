package ru.bpmcons.sbi_elma.models.dto.generralized;

import lombok.Data;

@Data
public
class Creator_editor {
    private String id_ecm_creator;
    private String id_as_creator;           // id автора в системе-источник
    private String app_id;
    private String app_sysname;
//    private String staff_number;
    private String fullname;     // полноое имя
    private String role;         // Роль автора документа
    private String email;
}