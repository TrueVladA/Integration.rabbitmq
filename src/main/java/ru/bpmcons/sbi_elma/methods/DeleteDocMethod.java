package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.EcmCreatorEditorService;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DulType;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataRow;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;
import ru.bpmcons.sbi_elma.ecm.dto.reference.CreatorEditor;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DulTypeRepository;
import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListRequest;
import ru.bpmcons.sbi_elma.exceptions.CheckRequiredParametersException;
import ru.bpmcons.sbi_elma.exceptions.DocumentArchivedException;
import ru.bpmcons.sbi_elma.exceptions.DocumentNotFoundException;
import ru.bpmcons.sbi_elma.exceptions.FilesNotFoundException;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.methods.additional.DeleteFileService;
import ru.bpmcons.sbi_elma.models.dto.DeleteFileContext;
import ru.bpmcons.sbi_elma.models.dto.RequestDeleteFile;
import ru.bpmcons.sbi_elma.models.dto.deleteFile.FileMetadataDelete;
import ru.bpmcons.sbi_elma.models.dto.deleteFile.RequestDeleteFileFromRabbit;
import ru.bpmcons.sbi_elma.models.dto.deleteFile.ResponseDeleteFileToMq;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromEcmCreateDoc;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.RequestFileMetadataContext;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.ResponseFromEcmListFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.ResponseFromEcmCreateIdentityDoc;
import ru.bpmcons.sbi_elma.models.dto.jwt.JwtToken;
import ru.bpmcons.sbi_elma.models.request.DeleteDocRequest;
import ru.bpmcons.sbi_elma.properties.EcmApiConst;
import ru.bpmcons.sbi_elma.properties.EcmProperties;
import ru.bpmcons.sbi_elma.properties.ResponseCodes;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.security.authorization.Authorized;
import ru.bpmcons.sbi_elma.security.file.authorization.FileAuthorizationService;
import ru.bpmcons.sbi_elma.service.LockService;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class DeleteDocMethod {
    public static final String SLASH = "/";

    private final ObjectMapperService objectMapperService;
    private final SysNamesConstants sysNamesConstants;
    private final EcmProperties ecmProperties;
    private final PublicApiElmaService publicApiElmaService;
    private final FileAuthorizationService fileAuthorizationService;
    private final LockService lockService;
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;
    private final EcmCreatorEditorService ecmCreatorEditorService;
    private final ContractTypeRepository contractTypeRepository;
    private final DulTypeRepository dulTypeRepository;
    private final DocTypeRepository docTypeRepository;
    private final DeleteFileService deleteFileService;
    private final MessageWorkerService messageWorkerService;

    @Validated
    @Method("${methods.deletedoc}")
    @Authorized(OperationName.DELETE)
    public ResponseDeleteFileToMq doMethod(@Valid DeleteDocRequest dRequest) {
        String type_sys_name = dRequest.getDocType().getDictValues()[0].getDictValue().getValue();
        lockService.checkLock(dRequest.getEcmId());

        FileAuthorizationService.Context fileAuthContext = fileAuthorizationService.buildContext(dRequest);

        RequestDeleteFileFromRabbit request = new RequestDeleteFileFromRabbit();
        request.setId_as_doc(dRequest.getAsId());
        request.setId_ecm_doc(dRequest.getEcmId());
        request.setEditor(dRequest.getEditor());
        request.setDoc_type(dRequest.getDocType());
        request.setRquid(dRequest.getRequestId());
        JwtToken jwtToken = new JwtToken();
        jwtToken.setAccess_token(dRequest.getJwtToken().getAccessToken());
        request.setJwt_token(jwtToken);

        Optional<DocType> docType = docTypeRepository.findBySysNameOptional(type_sys_name);
        if (docType.isPresent()) {
            String path = docType.get().getEcmDoc()[0].getCode();
            ResponseFromEcmCreateDoc ecmDoc = getEcmObject(request, path);
            if (ecmDoc.getItem() == null) {
                throw new DocumentNotFoundException(request);
            } else if (ecmDoc.getItem().isArchive()) {
                throw new DocumentArchivedException(request);
            }
            List<String> idsMetadataInDoc = new ArrayList<>();
            if (ecmDoc.getItem().getFile_metadata() != null && ecmDoc.getItem().getFile_metadata().getRows() != null) {
                idsMetadataInDoc = ecmDoc.getItem().getFile_metadata().getRows()
                        .stream()
                        .map(FileMetadataRow::getFileMetadata)
                        .collect(Collectors.toList());
            }
//            FileMetadata[] fileMetadataArray = createOrUpdateFileMetadata.getFileMetadataArray(ecmDoc.getItem().getFile_metadata().getRows(), messageProperties);
            RequestFileMetadataContext[] contextMetadata = getContextMetadata(idsMetadataInDoc);

            checkArchiveMetadata(contextMetadata);

            List<FileMetadata> fileMetadata = Arrays.stream(contextMetadata)
                    .map(createOrUpdateFileMetadata::getFileMetadataArray)
                    .collect(Collectors.toList());
            fileAuthContext.requirePermissions(fileMetadata, OperationName.DELETE);
            if (fileMetadata.isEmpty()) {
                request.setFile_metadata(new FileMetadata[]{});
            } else {
                int size = fileMetadata.size();
                request.setFile_metadata(fileMetadata.toArray(new FileMetadata[size]));
            }
//            TypeDocument typeDocument = commonMethods.createTypeDocumentBySysName(request.getDoc_type().getDictValues()[0].getValue(), messageProperties);
            messageWorkerService.forEachParallel(contextMetadata, deleteFileService::delete);
            String body = createRequestToEcm(request, ecmDoc);
            publicApiElmaService.doPost(body,
                    ecmProperties.getPathToDocuments(),
                    path + SLASH + ecmDoc.getItem().get__id(),
                    EcmApiConst.UPDATE
            );
            return sendResponse(request);
        }
        Optional<ContractType> contractType = contractTypeRepository.findByTypeSysNameOptional(type_sys_name);
        if (contractType.isPresent()) {
            String path = contractType.get().getEcmDoc()[0].getCode();
            ResponseFromEcmCreateDoc ecmDoc = getEcmObject(request, path);
            if (ecmDoc.getItem() == null) {
                throw new DocumentNotFoundException(request);
            } else if (ecmDoc.getItem().isArchive()) {
                throw new DocumentArchivedException(request);
            }
            List<String> idsMetadataInDoc = new ArrayList<>();
            if (ecmDoc.getItem().getFile_metadata() != null && ecmDoc.getItem().getFile_metadata().getRows() != null) {
                idsMetadataInDoc = ecmDoc.getItem().getFile_metadata().getRows()
                        .stream()
                        .map(FileMetadataRow::getFileMetadata)
                        .collect(Collectors.toList());
            }
//            FileMetadata[] fileMetadataArray = createOrUpdateFileMetadata.getFileMetadataArray(ecmDoc.getItem().getFile_metadata().getRows(), messageProperties);
            RequestFileMetadataContext[] contextMetadata = getContextMetadata(idsMetadataInDoc);

            checkArchiveMetadata(contextMetadata);

            List<FileMetadata> fileMetadata = Arrays.stream(contextMetadata)
                    .map(fileMetadataContext -> createOrUpdateFileMetadata.getFileMetadataArray(fileMetadataContext))
                    .collect(Collectors.toList());
            fileAuthContext.requirePermissions(fileMetadata, OperationName.DELETE);
            if (fileMetadata.isEmpty()) {
                request.setFile_metadata(new FileMetadata[]{});
            } else {
                int size = fileMetadata.size();
                request.setFile_metadata(fileMetadata.toArray(new FileMetadata[size]));
            }
            messageWorkerService.forEachParallel(contextMetadata, deleteFileService::delete);
            String body = createRequestToEcm(request, ecmDoc);
            publicApiElmaService.doPost(body,
                    ecmProperties.getPathToDocuments(),
                    path + SLASH + ecmDoc.getItem().get__id(),
                    EcmApiConst.UPDATE
            );
            return sendResponse(request);
        }
        Optional<DulType> dulType = dulTypeRepository.findByCodeOptional(type_sys_name);
        if (dulType.isPresent()) {
            String path = dulType.get().getCode();
            ResponseFromEcmCreateIdentityDoc ecmDoc = getIdentityDoc(request, path);
            if (ecmDoc.getItem() == null) {
                throw new DocumentNotFoundException(request);
            } else if (ecmDoc.getItem().isArchive()) {
                throw new DocumentArchivedException(request);
            }
            //TODO проверки ролей доступа для ДУЛ пока нет
//
            List<String> idsMetadataInDoc = new ArrayList<>();
            if (ecmDoc.getItem().getFile_metadata() != null && ecmDoc.getItem().getFile_metadata().getRows() != null) {
                idsMetadataInDoc = ecmDoc.getItem().getFile_metadata().getRows()
                        .stream()
                        .map(FileMetadataRow::getFileMetadata)
                        .collect(Collectors.toList());
            }
//            FileMetadata[] fileMetadataArray = createOrUpdateFileMetadata.getFileMetadataArray(ecmDoc.getItem().getFile_metadata().getRows(), messageProperties);
            RequestFileMetadataContext[] contextMetadata = getContextMetadata(idsMetadataInDoc);

            checkArchiveMetadata(contextMetadata);

            List<FileMetadata> fileMetadata = Arrays.stream(contextMetadata)
                    .map(createOrUpdateFileMetadata::getFileMetadataArray)
                    .collect(Collectors.toList());

            if (fileMetadata.isEmpty()) {
                request.setFile_metadata(new FileMetadata[]{});
            } else {
                int size = fileMetadata.size();
                request.setFile_metadata(fileMetadata.toArray(new FileMetadata[size]));
            }
            //TODO проверки ролей доступа для ДУЛ пока нет
            messageWorkerService.forEachParallel(contextMetadata, deleteFileService::delete);
            String body = createRequestToEcm(request, ecmDoc);
            publicApiElmaService.doPost(body,
                    ecmProperties.getPathToDocuments(),
                    path + SLASH + ecmDoc.getItem().get__id(),
                    EcmApiConst.UPDATE
            );
            return sendResponse(request);
        } else {
            throw new CheckRequiredParametersException(ResponseCodes.REQUIRED_VALUE_MISSING_INT, "Указанный тип документа не существует: " + request.getDoc_type().getDictValues()[0].getDictValue().getValue());
        }
    }

    private ResponseDeleteFileToMq sendResponse(RequestDeleteFileFromRabbit request) {
        String responseMessage = createResponseMessage(request);

        List<FileMetadataDelete> fileMetadataDeleteList = Arrays.stream(request.getFile_metadata()).map(fileMetadata -> FileMetadataDelete.builder().id_ecm_filemetadata(fileMetadata.getId_ecm_filemetadata()).id_as_filemetadata(fileMetadata.getId_as_filemetadata()).build()).collect(Collectors.toList());
        FileMetadataDelete[] fileMetadataDeletes;
        if (fileMetadataDeleteList.isEmpty()) {
            fileMetadataDeletes = new FileMetadataDelete[]{};
        } else {
            int size = fileMetadataDeleteList.size();
            fileMetadataDeletes = fileMetadataDeleteList.toArray(new FileMetadataDelete[size]);
        }

        ResponseDeleteFileToMq responseDeleteFileToMq = ResponseDeleteFileToMq.builder()
                .rquid(request.getRquid())
                .id_ecm_doc(request.getId_ecm_doc())
                .id_as_doc(request.getId_as_doc())
                .file_metadata(fileMetadataDeletes)
                .build();
        responseDeleteFileToMq.setResponse_code(ResponseCodes.OK);
        responseDeleteFileToMq.setResponse_message(responseMessage);
        return responseDeleteFileToMq;
    }

    private String createResponseMessage(RequestDeleteFileFromRabbit request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Документ ")
                .append(request.getDoc_type().getDictValues()[0].getDictValue().getValue())
                .append(", ID ").append(request.getId_ecm_doc())
                .append(" удалён.");
        builder.append(" Удалены файлы: ");
        if (request.getFile_metadata().length == 0) {
            builder.append("Запрашиваемых на удаление файлов в документе не обнаружено");
        } else {
            builder.append(" Удалены файлы: ");
            Arrays.stream(request.getFile_metadata()).forEach(fileMetadata -> {
                builder.append(fileMetadata.getFile_name())
                        .append(" ")
                        .append(fileMetadata.getId_ecm_filemetadata())
                        .append("; ");
            });
        }
        return builder.toString();
    }

    private String createRequestToEcm(RequestDeleteFileFromRabbit request, ResponseFromEcmCreateDoc ecmDoc) {
        RequestDeleteFile requestDeleteFile = new RequestDeleteFile();
        DeleteFileContext requestContext = new DeleteFileContext();
        FileMetadataTable fileMetadataTable = ecmDoc.getItem().getFile_metadata();
        requestContext.setFile_metadata(fileMetadataTable);
        CreatorEditor editor = ecmCreatorEditorService.findOrCreateEditor(request.getEditor());
        requestContext.setEditor(new String[]{editor.getId()});
        requestContext.set__id(ecmDoc.getItem().get__id());
        requestContext.setArchive(true);
        requestDeleteFile.setContext(requestContext);
        return objectMapperService.getJsonFromObjectRequired(requestDeleteFile);
    }

    private String createRequestToEcm(RequestDeleteFileFromRabbit request, ResponseFromEcmCreateIdentityDoc ecmDoc) {
        RequestDeleteFile requestDeleteFile = new RequestDeleteFile();
        DeleteFileContext requestContext = new DeleteFileContext();
        FileMetadataTable fileMetadataTable = ecmDoc.getItem().getFile_metadata();
        requestContext.setFile_metadata(fileMetadataTable);
        CreatorEditor editor = ecmCreatorEditorService.findOrCreateEditor(request.getEditor());
        requestContext.setEditor(new String[]{editor.getId()});
        requestContext.set__id(ecmDoc.getItem().get__id());
        requestContext.setArchive(true);
        requestDeleteFile.setContext(requestContext);
        return objectMapperService.getJsonFromObjectRequired(requestDeleteFile);
    }

    private RequestFileMetadataContext[] getContextMetadata(List<String> requestIdMetadata) {
        String body = createJsonForEcmRequest(requestIdMetadata);
        ResponseEntity<String> response = publicApiElmaService.doPost(body,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getFileMetadata(),
                EcmApiConst.LIST
        );
        String responseBody = response.getBody();
        ResponseFromEcmListFileMetadata responseObject = objectMapperService.getObjectFromJsonRequired(responseBody,
                ResponseFromEcmListFileMetadata.class);
        return responseObject.getResult().getResult();
    }

    private String createJsonForEcmRequest(List<String> requestIdMetadata) {
        return objectMapperService.getJsonFromObjectRequired(new ElmaListRequest().ids(requestIdMetadata));
    }

    private void checkArchiveMetadata(RequestFileMetadataContext[] fileMetadataArray) {
        List<RequestFileMetadataContext> archiveMetadata = Arrays.stream(fileMetadataArray).filter(RequestFileMetadataContext::isArchive).collect(Collectors.toList());
        if (!archiveMetadata.isEmpty()) {
            throw new FilesNotFoundException(
                    archiveMetadata.stream()
                            .map(RequestFileMetadataContext::get__id)
                            .collect(Collectors.toList())
            );
        }
    }

    private ResponseFromEcmCreateDoc getEcmObject(RequestDeleteFileFromRabbit request, String path) {
        ResponseEntity<String> responseEntity = publicApiElmaService.doPost("",
                ecmProperties.getPathToDocuments(),
                path + SLASH + request.getId_ecm_doc(),
                EcmApiConst.GET
        );
        return objectMapperService.getObjectFromJsonRequired(responseEntity.getBody(),
                ResponseFromEcmCreateDoc.class);
    }

    private ResponseFromEcmCreateIdentityDoc getIdentityDoc(RequestDeleteFileFromRabbit request, String path) {
        ResponseEntity<String> responseEntity = publicApiElmaService.doPost("",
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getDul() + SLASH + request.getId_ecm_doc(),
                EcmApiConst.GET
        );
        return objectMapperService.getObjectFromJsonRequired(responseEntity.getBody(),
                ResponseFromEcmCreateIdentityDoc.class);
    }
}
