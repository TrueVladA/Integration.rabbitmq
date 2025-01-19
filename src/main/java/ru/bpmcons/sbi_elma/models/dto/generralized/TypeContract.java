package ru.bpmcons.sbi_elma.models.dto.generralized;

import lombok.Data;

@Data
public class TypeContract {

//    public String type_id;          // ID типа в системе-источнике
    private String type_sys_name;    // Системное имя типа
    private String type_bus_name;    // Бизнес-имя типа
}
