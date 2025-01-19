package ru.bpmcons.sbi_elma.models.dto.searchDoc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.models.dto.responseMq.CodeMessage;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseSearchDoc extends CodeMessage {
    private GeneralizedDoc[] documents;
}
