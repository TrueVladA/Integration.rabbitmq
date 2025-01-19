package ru.bpmcons.sbi_elma.models.dto.creator;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.doc.Fio;

@Data
public class Item {
    private String __id;
    private String id_ecm_creator;
    private String id_as_creator;
    private String[] source;
//    private String staff_number;
    private Fio fio;
    private String role;
    private EcmEmail[] email;
    private String __name;
}
