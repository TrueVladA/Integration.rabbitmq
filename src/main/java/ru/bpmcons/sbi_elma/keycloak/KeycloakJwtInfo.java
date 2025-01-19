package ru.bpmcons.sbi_elma.keycloak;

import lombok.Data;

@Data
public class KeycloakJwtInfo {
    private final String[] roles;
    private final String email;
    private final String name;
}
