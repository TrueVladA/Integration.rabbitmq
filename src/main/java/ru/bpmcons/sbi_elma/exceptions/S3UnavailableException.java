package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class S3UnavailableException extends ServiceResponseException {
    public S3UnavailableException(String responseMessage) {
        super(HttpStatus.SERVICE_UNAVAILABLE.value(), responseMessage);
    }
}
