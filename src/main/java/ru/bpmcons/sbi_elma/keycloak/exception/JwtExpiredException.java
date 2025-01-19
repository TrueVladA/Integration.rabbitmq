package ru.bpmcons.sbi_elma.keycloak.exception;

import ru.bpmcons.sbi_elma.exceptions.ServiceResponseException;
import ru.bpmcons.sbi_elma.properties.ResponseCodes;

public class JwtExpiredException extends ServiceResponseException {
    public JwtExpiredException() {
        super(ResponseCodes.JWT_UPGRADE_REQUIRED_INT, "Действие jwt токена для пользователя истекло. Сделайте новый запрос в keycloak");
    }
}
