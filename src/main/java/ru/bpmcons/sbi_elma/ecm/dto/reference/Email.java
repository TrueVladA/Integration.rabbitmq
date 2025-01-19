package ru.bpmcons.sbi_elma.ecm.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {
    private String type;
    private String email;

    public static Email mainEmail(String email) {
        return new Email("main", email);
    }
}
