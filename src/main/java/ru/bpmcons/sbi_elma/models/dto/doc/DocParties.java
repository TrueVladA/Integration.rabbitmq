package ru.bpmcons.sbi_elma.models.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class DocParties {
    private DocPartiesRow[] rows;
}
