package ru.bpmcons.sbi_elma.infra.method.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class MethodNotFoundException extends ServiceResponseException {
    public MethodNotFoundException(String methodName) {
        super(HttpStatus.BAD_REQUEST.value(), "Метода " + methodName + " не существует");
    }
}
