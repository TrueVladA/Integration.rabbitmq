package ru.bpmcons.sbi_elma.security;

import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.ecm.dto.dict.CommonSystem;
import ru.bpmcons.sbi_elma.keycloak.KeycloakJwtInfo;

@Data
public class SecurityContext {
    @NonNull
    private final CommonSystem system;
    @Nullable
    private final KeycloakJwtInfo principal;
}
