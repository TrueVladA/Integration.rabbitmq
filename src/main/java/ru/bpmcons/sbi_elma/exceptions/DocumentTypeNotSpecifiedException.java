package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class DocumentTypeNotSpecifiedException extends ServiceResponseException {
    public DocumentTypeNotSpecifiedException() {
        super(HttpStatus.BAD_REQUEST.value(), "Не указан doc_type И contract_type");
    }
}
