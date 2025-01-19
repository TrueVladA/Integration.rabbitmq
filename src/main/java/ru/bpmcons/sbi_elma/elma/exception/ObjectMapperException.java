package ru.bpmcons.sbi_elma.elma.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

import java.io.IOException;

public class ObjectMapperException extends ServiceResponseException {
    protected ObjectMapperException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    public ObjectMapperException(IOException e) {
        this(e.getMessage());
    }
}
