package ru.bpmcons.sbi_elma.elma.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class ElmaException extends ServiceResponseException {
    public ElmaException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }
}
