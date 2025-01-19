package ru.bpmcons.sbi_elma.ecm.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class DocTypeNotSupportedException extends ServiceResponseException {
    public DocTypeNotSupportedException(String docType) {
        super(HttpStatus.BAD_REQUEST.value(), "Тип документа " + docType + " не поддерживается");
    }
}
