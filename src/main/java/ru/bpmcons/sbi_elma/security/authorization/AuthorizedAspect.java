package ru.bpmcons.sbi_elma.security.authorization;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.exceptions.CheckPermissionException;
import ru.bpmcons.sbi_elma.exceptions.ForbiddenException;
import ru.bpmcons.sbi_elma.keycloak.KeycloakJwtInfo;
import ru.bpmcons.sbi_elma.models.ability.ContractTyped;
import ru.bpmcons.sbi_elma.models.ability.DocumentTyped;
import ru.bpmcons.sbi_elma.models.ability.EntityTyped;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.properties.SettingsProperties;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizedAspect {
    private final SettingsProperties settingsProperties;
    private final DocumentAuthorizationService documentAuthorizationService;

    @Before("@annotation(authorized) && args(arg, ..)")
    public void authorize(Authorized authorized, EntityTyped arg) {
        OperationName operationName = authorized.value();
        if (settingsProperties.getPermissionValidation() == SettingsProperties.PermissionValidation.NONE) {
            return;
        }

        KeycloakJwtInfo principal = SecurityContextHolder.getRequiredPrincipal();
        if (principal == null) {
            throw new ForbiddenException("Субъект не инициализирован");
        }

        checkAccess(arg, principal, operationName);
    }

    private void checkAccess(EntityTyped arg, KeycloakJwtInfo principal, OperationName operationName) {
        if (checkDocument(arg, principal, operationName)) return;
        if (checkContract(arg, principal, operationName)) return;
        throw new CheckPermissionException("У пользователя " + principal.getName() +
                " c email = " + principal.getEmail() + " нет прав на документ без типов. Запрос отменён.");
    }

    private boolean checkDocument(EntityTyped arg, KeycloakJwtInfo principal, OperationName operationName) {
        if (arg instanceof DocumentTyped) {
            PerecoderObject docType = ((DocumentTyped) arg).getDocType();
            return documentAuthorizationService.verifyDocType(docType, principal, operationName);
        }
        return false;
    }

    private boolean checkContract(EntityTyped arg, KeycloakJwtInfo principal, OperationName operationName) {
        if (arg instanceof ContractTyped) {
            PerecoderObject contractType = ((ContractTyped) arg).getContractType();
            return documentAuthorizationService.verifyContractType(contractType, principal, operationName);
        }
        return false;
    }
}
