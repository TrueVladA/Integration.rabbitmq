package ru.bpmcons.sbi_elma.exceptions;

import ru.bpmcons.sbi_elma.properties.ResponseCodes;

public class WrongSysNameException extends ServiceResponseException {
    public WrongSysNameException(String responseMessage) {
        super(ResponseCodes.REQUIRED_VALUE_MISSING_INT, responseMessage);
    }
}
