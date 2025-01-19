package ru.bpmcons.sbi_elma.keycloak.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

public class JwtAudienceInvalidException extends ServiceResponseException {
    public JwtAudienceInvalidException() {
        super(HttpStatus.FORBIDDEN.value(), "Аудиенция (aud) JWT-токена не позволяет его использовать. Выпустите токен с верной аудиенцией");
    }
}
