package ru.bpmcons.sbi_elma.security.file.authorization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.FileType;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.repository.RoleRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.FileTypeRepository;
import ru.bpmcons.sbi_elma.exceptions.CheckRequiredParametersException;
import ru.bpmcons.sbi_elma.exceptions.ForbiddenException;
import ru.bpmcons.sbi_elma.keycloak.KeycloakJwtInfo;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.request.DeleteFileRequest;
import ru.bpmcons.sbi_elma.properties.SettingsProperties;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileAuthorizationService {
    private final DocTypeRepository docTypeRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final RoleRepository roleRepository;
    private final FileTypeRepository fileTypeRepository;
    private final SettingsProperties settingsProperties;

    public Context buildContext(Identifiable identifiable) {
        if (settingsProperties.getPermissionValidation() == SettingsProperties.PermissionValidation.NONE) {
            return new NoopContext();
        }
        KeycloakJwtInfo principal = SecurityContextHolder.getRequiredPrincipal();
        if (identifiable.getDocType() != null && identifiable.getDocType().valid()) {
            DocType type = docTypeRepository.findBySysName(identifiable.getDocType());
            return new DocumentContext(type, principal);
        } else if (identifiable.getContractType() != null && identifiable.getContractType().valid()) {
            ContractType contractType = contractTypeRepository.findByTypeSysName(identifiable.getContractType());
            return new ContractContext(contractType, principal);
        } else {
            throw new CheckRequiredParametersException(400, "Документ должен содержать тип");
        }
    }

    public Context buildContext(DocType docType, ContractType contractType) {
        if (settingsProperties.getPermissionValidation() == SettingsProperties.PermissionValidation.NONE) {
            return new NoopContext();
        }
        KeycloakJwtInfo principal = SecurityContextHolder.getRequiredPrincipal();
        if (docType != null) {
            return new DocumentContext(docType, principal);
        } else if (contractType != null) {
            return new ContractContext(contractType, principal);
        } else {
            throw new CheckRequiredParametersException(400, "Документ должен содержать тип");
        }
    }

    public Context buildNoopContext() {
        return new NoopContext();
    }

    public interface Context {
        KeycloakJwtInfo getPrincipal();

        boolean checkPermission(String fileType, OperationName operationName);

        default void requirePermission(FileType fileType, OperationName operationName) {
            if (!checkPermission(fileType.getFileTypeId(), operationName)) {
                throw new ForbiddenException("У пользователя " + getPrincipal().getName() +
                        " c email = " + getPrincipal().getEmail() + " нет прав на операцию с файлами " + fileType.getFileTypeName());
            }
        }
        default void requirePermission(FileMetadata metadata, OperationName operationName) {
            if (!checkPermission(metadata.getFile_type().getSingleValue(), operationName)) {
                throw new ForbiddenException("У пользователя " + getPrincipal().getName() +
                        " c email = " + getPrincipal().getEmail() + " нет прав на операцию с файлами с id_as: " + metadata.getId_as_filemetadata() + " (" + metadata.getFile_type().getSingleValue() + ")");
            }
        }
        default void requirePermissions(List<FileMetadata> metadata, OperationName operationName) {
            String files = metadata.stream()
                    .filter(meta -> !checkPermission(meta.getFile_type().getSingleValue(), operationName))
                    .map(meta -> meta.getId_as_filemetadata() + " (" + meta.getFile_type().getSingleValue() + ") ")
                    .collect(Collectors.joining(", "));
            if (!files.isBlank()) {
                throw new ForbiddenException("У пользователя " + getPrincipal().getName() +
                        " c email = " + getPrincipal().getEmail() + " нет прав на операцию с файлами с id_as: " + files);
            }
        }

        default void requireRefPermissions(List<DeleteFileRequest.FileMetadataRef> metadata, OperationName operationName) {
            String files = metadata.stream()
                    .filter(meta -> !checkPermission(meta.getFileType().getSingleValue(), operationName))
                    .map(meta -> meta.getAsId() + " (" + meta.getFileType().getSingleValue() + ") ")
                    .collect(Collectors.joining(", "));
            if (!files.isBlank()) {
                throw new ForbiddenException("У пользователя " + getPrincipal().getName() +
                        " c email = " + getPrincipal().getEmail() + " нет прав на операцию с файлами с id_as: " + files);
            }
        }
    }

    private static class NoopContext implements Context {
        @Override
        public KeycloakJwtInfo getPrincipal() {
            return null;
        }

        @Override
        public boolean checkPermission(String fileType, OperationName operationName) {
            return true;
        }
    }

    @RequiredArgsConstructor
    private class DocumentContext implements Context {
        private final DocType docType;
        @Getter
        private final KeycloakJwtInfo principal;

        @Override
        public boolean checkPermission(String fileType, OperationName operationName) {
            FileType fileTypeObject = fileTypeRepository.findByFileTypeId(fileType);
            return roleRepository.findDocFileRole(
                            docType.getId(),
                            fileTypeObject,
                            new ArrayList<>(List.of(principal.getRoles())),
                            operationName)
                    .isPresent();
        }
    }

    @RequiredArgsConstructor
    private class ContractContext implements Context {
        private final ContractType contractType;
        @Getter
        private final KeycloakJwtInfo principal;

        @Override
        public boolean checkPermission(String fileType, OperationName operationName) {
            FileType fileTypeObject = fileTypeRepository.findByFileTypeId(fileType);
            return roleRepository.findContractFileRole(
                            contractType.getId(),
                            fileTypeObject,
                            new ArrayList<>(List.of(principal.getRoles())),
                            operationName)
                    .isPresent();
        }
    }
}
