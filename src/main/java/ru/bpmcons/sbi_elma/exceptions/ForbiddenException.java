package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ServiceResponseException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN.value(), message);
    }
}
