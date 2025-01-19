package ru.bpmcons.sbi_elma.models.dto.identityDoc;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;

@Data
public class ResponseFromEcmListIdentityDoc extends ResponseHeaderFromEcm {
    private Result result;
}
