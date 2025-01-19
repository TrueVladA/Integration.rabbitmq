package ru.bpmcons.sbi_elma.models.dto.doc;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Data
public class DocPartiesRow {
    private String dul;     //Строка dul
    private Fio fio;
    private String inn;
    private String id_as;
    private String[] identitydoc;
    private Date birthdate;
    private String[] opf;
    private String[] vip;
    private String full_name;
    private String[] party_role;
    private String[] party_type;
    private String short_name;
    private String email;
}
