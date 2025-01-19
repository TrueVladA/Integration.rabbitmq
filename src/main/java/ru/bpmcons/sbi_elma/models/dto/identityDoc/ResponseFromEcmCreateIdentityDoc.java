package ru.bpmcons.sbi_elma.models.dto.identityDoc;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;

@Data
public class ResponseFromEcmCreateIdentityDoc extends ResponseHeaderFromEcm {
    private RequestIdentityDocContext item;
}
