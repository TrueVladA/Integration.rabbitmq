package ru.bpmcons.sbi_elma.keycloak.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class JwtSignatureInvalidException extends ServiceResponseException {
    public JwtSignatureInvalidException() {
        super(HttpStatus.FORBIDDEN.value(), "Ошибка валидации signature jwt токена. Обратитесь к администратору keycloak.");
    }
}
