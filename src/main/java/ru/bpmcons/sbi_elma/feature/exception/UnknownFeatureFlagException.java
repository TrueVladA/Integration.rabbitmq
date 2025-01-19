package ru.bpmcons.sbi_elma.feature.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class UnknownFeatureFlagException extends ServiceResponseException {
    public UnknownFeatureFlagException(String flag) {
        super(HttpStatus.BAD_REQUEST.value(), "неизвестный фича-флаг: " + flag);
    }
}
