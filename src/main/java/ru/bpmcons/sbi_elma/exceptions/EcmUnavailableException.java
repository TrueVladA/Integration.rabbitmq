package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class EcmUnavailableException extends ServiceResponseException {
    public EcmUnavailableException(String responseMessage) {
        super(HttpStatus.GATEWAY_TIMEOUT.value(), responseMessage);
    }
}
