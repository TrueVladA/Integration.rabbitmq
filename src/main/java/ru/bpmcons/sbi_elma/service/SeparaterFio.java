package ru.bpmcons.sbi_elma.service;

import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.exceptions.EmptyFullNameException;
import ru.bpmcons.sbi_elma.models.dto.doc.Fio;

@Service
public class SeparaterFio {

    @Deprecated
    public Fio separateFio(String fullname) {
        Fio fio = new Fio();
        if (fullname == null || fullname.isEmpty() || fullname.isBlank()) {
            throw new EmptyFullNameException();
        }
        String[] s = fullname.split(" ");
        if (s.length == 1) {
            fio.setFirstname(s[0]);
        }
        else if (s.length == 2) {
            fio.setLastname(s[0]);
            fio.setFirstname(s[1]);
        }
        else if (s.length >= 3) {
            fio.setLastname(s[0]);
            fio.setFirstname(s[1]);
            fio.setMiddlename(s[2]);
        }
        return fio;
    }

    public ru.bpmcons.sbi_elma.ecm.dto.reference.Fio separate(String fullname) {
        Fio fio1 = separateFio(fullname);
        ru.bpmcons.sbi_elma.ecm.dto.reference.Fio fio = new ru.bpmcons.sbi_elma.ecm.dto.reference.Fio();
        fio.setFirstname(fio1.getFirstname());
        fio.setLastname(fio1.getLastname());
        fio.setMiddlename(fio1.getMiddlename());
        return fio;
    }
}
