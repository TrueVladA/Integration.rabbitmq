package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.models.dto.deleteFile.RequestDeleteFileFromRabbit;

public class DocumentArchivedException extends EcmDocumentAwareException {

    public DocumentArchivedException(RequestDeleteFileFromRabbit doc) {
        super(HttpStatus.CONFLICT.value(), "Документ с id_as = " + doc.getId_as_doc() + ", "
                + " и видом " + "\"" + ((doc.getDoc_type() != null && doc.getDoc_type().valid()) ? doc.getDoc_type().getDictValues()[0].getDictValue().getValue() : null) + "\""
                + " (id_ecm = " + doc.getId_ecm_doc() + " ) "
                + " находится в архиве. Изменять архивный документ запрещено",
                doc.getId_ecm_doc(),
                doc.getId_as_doc());
    }
}
