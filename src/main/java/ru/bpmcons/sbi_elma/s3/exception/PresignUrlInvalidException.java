package ru.bpmcons.sbi_elma.s3.exception;

import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class PresignUrlInvalidException extends ServiceResponseException {
    public PresignUrlInvalidException() {
        super(460, "Presign-ссылка неверна");
    }
}
