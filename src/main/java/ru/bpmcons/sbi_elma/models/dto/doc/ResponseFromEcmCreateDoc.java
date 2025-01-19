package ru.bpmcons.sbi_elma.models.dto.doc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseFromEcmCreateDoc extends ResponseHeaderFromEcm {
    private ResponseFromPublicApi item;
}
