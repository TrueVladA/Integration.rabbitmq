package ru.bpmcons.sbi_elma.exceptions;

public class CheckRequiredParametersException extends ServiceResponseException {
    public CheckRequiredParametersException(int code, String responseMessage) {
        super(code, responseMessage);
    }
}
