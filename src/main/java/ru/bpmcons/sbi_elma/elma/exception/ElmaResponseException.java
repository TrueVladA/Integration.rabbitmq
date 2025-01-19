package ru.bpmcons.sbi_elma.elma.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class ElmaResponseException extends ServiceResponseException {
    public ElmaResponseException(int code, String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ответ от ELMA с кодом " + code + (message == null ? "" : " с телом " + message));
    }
}
