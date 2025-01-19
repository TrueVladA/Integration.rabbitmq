package ru.bpmcons.sbi_elma.exceptions;

import lombok.Getter;

@Getter
public abstract class ServiceResponseException extends RuntimeException {
    private final int code;
    private final String message;

    protected ServiceResponseException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
