package ru.bpmcons.sbi_elma.models.dto.docLock;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;

@Data
public class ResponseFromEcmListBlock extends ResponseHeaderFromEcm {
    private Result result;
}
