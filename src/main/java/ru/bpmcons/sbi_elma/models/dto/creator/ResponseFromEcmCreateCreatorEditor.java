package ru.bpmcons.sbi_elma.models.dto.creator;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;

@Data
public class ResponseFromEcmCreateCreatorEditor extends ResponseHeaderFromEcm {
    private Item item;
}
