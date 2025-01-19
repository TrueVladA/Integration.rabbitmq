package ru.bpmcons.sbi_elma.models.dto.contract;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromPublicApi;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseFromEcmCreateContract extends ResponseHeaderFromEcm {
    private ResponseFromPublicApi item;
}
