package ru.bpmcons.sbi_elma.models.dto.doc;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;

@Data
public class ResponseFromEcmListDoc extends ResponseHeaderFromEcm {
    private Result result;
}
