package ru.bpmcons.sbi_elma.keycloak.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class JwtCommonException extends ServiceResponseException {
    public JwtCommonException(JwtException e) {
        super(HttpStatus.FORBIDDEN.value(), "Ошибка обработки JWT: " + e.getLocalizedMessage());
    }
}
