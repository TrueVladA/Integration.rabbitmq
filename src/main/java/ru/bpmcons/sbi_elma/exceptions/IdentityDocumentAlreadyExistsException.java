package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.models.request.CreateIdentityDocRequest;

public class IdentityDocumentAlreadyExistsException extends ServiceResponseException {
    public IdentityDocumentAlreadyExistsException(CreateIdentityDocRequest doc) {
        super(HttpStatus.CONFLICT.value(), "ДУЛ с id_as = " + doc.getAsId()
                + " и видом " + "\"" + ((doc.getCode() != null && doc.getCode().valid()) ? doc.getCode().getDictValues()[0].getDictValue().getValue() : null) + "\""
                + " уже существует. Рекомендуется метод updateIdentityDoc");
    }
}
