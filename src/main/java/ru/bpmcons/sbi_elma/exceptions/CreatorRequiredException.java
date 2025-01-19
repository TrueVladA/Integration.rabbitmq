package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class CreatorRequiredException extends ServiceResponseException{
    public CreatorRequiredException() {
        super(HttpStatus.BAD_REQUEST.value(), "Для создания документа обязательно поле creator");
    }
}
