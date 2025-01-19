package ru.bpmcons.sbi_elma.keycloak.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class JwtInvalidException extends ServiceResponseException {
    public JwtInvalidException() {
        super(HttpStatus.FORBIDDEN.value(), "Ошибка в структуре access_token. access_token должен содержать 3 части, разделённых запятой. ");
    }
}
