package ru.bpmcons.sbi_elma.security.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.repository.RoleRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.exceptions.CheckPermissionException;
import ru.bpmcons.sbi_elma.keycloak.KeycloakJwtInfo;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.properties.SettingsProperties;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentAuthorizationService {
    private final DocTypeRepository docTypeRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final RoleRepository roleRepository;
    private final SettingsProperties settingsProperties;
    private final SysNamesConstants constants;

    public void verifyCurrentDoc(@Nullable DocType type, @Nullable ContractType contractType, OperationName operationName) {
        if (settingsProperties.getPermissionValidation() == SettingsProperties.PermissionValidation.NONE) {
            return;
        }

        KeycloakJwtInfo principal = SecurityContextHolder.getRequiredPrincipal();
        if (type == null || type.getEcmDoc()[0].getCode().equals(constants.getContract())) {
            if (!verifyContractType(contractType, principal, operationName)) {
                throw new CheckPermissionException("У пользователя " + principal.getName() +
                        " c email = " + principal.getEmail() + " нет прав на договор. Запрос отменён.");
            }
        } else {
            if (!verifyDocType(type, principal, operationName)) {
                throw new CheckPermissionException("У пользователя " + principal.getName() +
                        " c email = " + principal.getEmail() + " нет прав на документ. Запрос отменён.");
            }
        }
    }

    public boolean verifyDocType(@Nullable PerecoderObject docType, KeycloakJwtInfo principal, OperationName operationName) {
        if (docType != null && docType.valid()) {
            DocType type = docTypeRepository.findBySysName(docType);
            return verifyDocType(type, principal, operationName);
        }
        return false;
    }

    public boolean verifyDocType(@Nullable DocType type, KeycloakJwtInfo principal, OperationName operationName) {
        if (type == null) {
            return false;
        }
        if (roleRepository.findDocRole(
                type.getId(),
                new ArrayList<>(List.of(principal.getRoles())),
                operationName
        ).isEmpty()) {
            throw new CheckPermissionException("У пользователя " + principal.getName() +
                    " c email = " + principal.getEmail() + " нет прав на документ с типом " + type.getSysName() + ". Запрос отменён.");
        } else {
            return true;
        }
    }


    public boolean verifyContractType(@Nullable PerecoderObject contractType, KeycloakJwtInfo principal, OperationName operationName) {
        if (contractType != null && contractType.valid()) {
            ContractType type = contractTypeRepository.findByTypeSysName(contractType);
            return verifyContractType(type, principal, operationName);
        }
        return false;
    }

    public boolean verifyContractType(@Nullable ContractType type, KeycloakJwtInfo principal, OperationName operationName) {
        if (type == null) {
            return false;
        }
        if (roleRepository.findContractRole(
                type.getId(),
                new ArrayList<>(List.of(principal.getRoles())),
                operationName
        ).isEmpty()) {
            throw new CheckPermissionException("У пользователя " + principal.getName() +
                    " c email = " + principal.getEmail() + " нет прав на контракт с типом " + type.getTypeSysName() + ". Запрос отменён.");
        } else {
            return true;
        }
    }


}
