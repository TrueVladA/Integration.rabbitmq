package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class EditorRequiredException extends ServiceResponseException{
    public EditorRequiredException() {
        super(HttpStatus.BAD_REQUEST.value(), "Для редактирования документа обязательно поле creator");
    }
}
