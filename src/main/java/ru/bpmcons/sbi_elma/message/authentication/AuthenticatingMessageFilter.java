package ru.bpmcons.sbi_elma.message.authentication;

import lombok.RequiredArgsConstructor;
import ru.bpmcons.sbi_elma.infra.message.MessageFilter;
import ru.bpmcons.sbi_elma.keycloak.KeycloakJwtInfo;
import ru.bpmcons.sbi_elma.keycloak.KeycloakJwtParser;
import ru.bpmcons.sbi_elma.properties.SettingsProperties;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

/**
 * Фильтр парсит JWT в теле запроса
 */
@RequiredArgsConstructor
public class AuthenticatingMessageFilter implements MessageFilter<Object, Object> {
    private final KeycloakJwtParser parser;
    private final SettingsProperties settingsProperties;

    @Override
    public Object filter(Object message) {
        if (settingsProperties.getPermissionValidation() == SettingsProperties.PermissionValidation.NONE) {
            return message;
        }

        if (message instanceof JwtTokenContainer) {
            KeycloakJwtInfo principal = parser.parse(
                    ((JwtTokenContainer) message).getJwtToken().getAccessToken(),
                    SecurityContextHolder.getRequiredContext().getSystem(),
                    settingsProperties.getPermissionValidation() == SettingsProperties.PermissionValidation.PARTIAL
            );
            SecurityContextHolder.setPrincipal(principal);
        }
        return message;
    }
}
