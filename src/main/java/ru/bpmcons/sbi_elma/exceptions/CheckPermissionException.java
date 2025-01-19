package ru.bpmcons.sbi_elma.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CheckPermissionException extends ServiceResponseException {
    public CheckPermissionException(String responseMessage) {
        super(HttpStatus.FORBIDDEN.value(), responseMessage);
    }
}
