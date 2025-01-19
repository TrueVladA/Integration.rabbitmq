package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

public class DocumentAlreadyExistsException extends EcmDocumentAwareException {
    public DocumentAlreadyExistsException(GeneralizedDoc doc, String ecmId) {
        super(HttpStatus.CONFLICT.value(), "Такой документ уже существует (full_doc_number = '" + doc.getDoc_full_number() + "', id_as = '" + doc.getId_as_doc() + "')", ecmId, doc.getId_as_doc());
    }
}
