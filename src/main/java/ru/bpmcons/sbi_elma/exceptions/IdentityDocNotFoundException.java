package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class IdentityDocNotFoundException extends EcmDocumentAwareException {
    public IdentityDocNotFoundException(String ecmId) {
        super(HttpStatus.CONFLICT.value(), "Документ, удостоверяющий личность, с id " + ecmId + ") не существует", ecmId, null);
    }
}
