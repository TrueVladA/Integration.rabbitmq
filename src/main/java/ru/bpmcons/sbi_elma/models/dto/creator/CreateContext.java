package ru.bpmcons.sbi_elma.models.dto.creator;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.doc.Fio;

@Data
public class CreateContext {
//    private String id_ecm_creator;
    private String id_as_creator;
    private String[] source;
//    private String staff_number;
    private Fio fio;
    private String role;
    private EcmEmail[] email;
}
