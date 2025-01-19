package ru.bpmcons.sbi_elma.models.dto.creator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EcmEmail {
    private String type;
    private String email;
}
