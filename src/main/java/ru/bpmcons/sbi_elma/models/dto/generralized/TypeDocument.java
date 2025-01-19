package ru.bpmcons.sbi_elma.models.dto.generralized;

import lombok.Data;

@Data
public class TypeDocument {

    private String type_sys_name;   // Внутреннее название типа документа в системе источнике.
    private String type_bus_name;   // Бизнес название типа документа, которое видит пользователь
}
