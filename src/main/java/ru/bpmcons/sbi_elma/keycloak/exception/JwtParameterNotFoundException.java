package ru.bpmcons.sbi_elma.keycloak.exception;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;

import java.util.List;

public class JwtParameterNotFoundException extends ServiceResponseException {
    public JwtParameterNotFoundException(List<String> parameters) {
        super(HttpStatus.BAD_REQUEST.value(), "В access_token отсутствуют необходимые параметры: " + parameters.stream().reduce((s, s2) -> s + ", " + s2).orElse(""));
    }
}
