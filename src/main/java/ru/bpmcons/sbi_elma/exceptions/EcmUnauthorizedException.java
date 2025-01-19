package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;

public class EcmUnauthorizedException extends ServiceResponseException {
    public EcmUnauthorizedException() {
        super(HttpStatus.BAD_GATEWAY.value(), "Ошибка авторизации в ECM. Токен невалиден");
    }
}
