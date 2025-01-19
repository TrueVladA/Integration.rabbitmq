package ru.bpmcons.sbi_elma.infra.version.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class InvalidVersionException extends ServiceResponseException {
    public InvalidVersionException(String message) {
        super(HttpStatus.BAD_REQUEST.value(), "Формат версии неверен: " + message);
    }
}
