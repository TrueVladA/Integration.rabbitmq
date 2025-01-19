package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class EmptyFullNameException extends ServiceResponseException {
    public EmptyFullNameException() {
        super(HttpStatus.BAD_REQUEST.value(), "Пустое значение поля fullname");
    }
}
