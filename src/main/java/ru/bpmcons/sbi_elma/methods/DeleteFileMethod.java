package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DulType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.FileType;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataRow;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DulTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.FileTypeRepository;
import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListRequest;
import ru.bpmcons.sbi_elma.exceptions.CheckRequiredParametersException;
import ru.bpmcons.sbi_elma.exceptions.ContractNotFoundException;
import ru.bpmcons.sbi_elma.exceptions.DocumentNotFoundException;
import ru.bpmcons.sbi_elma.exceptions.FilesNotFoundException;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.methods.additional.CommonMethods;
import ru.bpmcons.sbi_elma.methods.additional.DeleteFileService;
import ru.bpmcons.sbi_elma.models.dto.deleteFile.FileMetadataDelete;
import ru.bpmcons.sbi_elma.models.dto.deleteFile.ResponseDeleteFileToMq;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromEcmCreateDoc;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.RequestFileMetadataContext;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.ResponseFromEcmListFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.models.request.DeleteFileRequest;
import ru.bpmcons.sbi_elma.properties.*;
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
public class DeleteFileMethod {
    public static final String SLASH = "/";

    private final ObjectMapperService objectMapperService;
    private final SysNamesConstants sysNamesConstants;
    private final EcmProperties ecmProperties;
    private final PublicApiElmaService publicApiElmaService;
    private final FileAuthorizationService fileAuthorizationService;
    private final LockService lockService;
    private final FileTypeRepository fileTypeRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final DulTypeRepository dulTypeRepository;
    private final DocTypeRepository docTypeRepository;
    private final DeleteFileService deleteFileService;
    private final MessageWorkerService messageWorkerService;

    @Validated
    @Method("${methods.deletefile}")
    public ResponseDeleteFileToMq doMethod(@Valid DeleteFileRequest request) {
        List<String> fileIds = request.getFileMetadata().stream().map(DeleteFileRequest.FileMetadataRef::getEcmId).collect(Collectors.toList());
        lockService.checkLockOnFiles(request.getEcmId(), fileIds);
        FileAuthorizationService.Context fileAuthContext = fileAuthorizationService.buildContext(request);
        String sysNameOfDocument = request.getDocType().getDictValues()[0].getDictValue().getValue();
        Optional<DocType> docType = docTypeRepository.findBySysNameOptional(sysNameOfDocument);
        if (docType.isPresent()) {
            //Удаление файла из документа
            String path = docType.get().getEcmDoc()[0].getCode();
            ResponseFromEcmCreateDoc ecmDoc = getEcmObject(request, path);
            if (ecmDoc.getItem() == null) {
                throw new DocumentNotFoundException(docType.get(), request.getEcmId(), request.getAsId());
            }
            RequestFileMetadataContext[] contextMetadata = getContextMetadata(fileIds);
            checkMetadata(new ArrayList<>(fileIds), contextMetadata);
            checkMetadataInDoc(new ArrayList<>(fileIds), ecmDoc);
            setFileType(request, contextMetadata);
//            TypeDocument typeDocument = commonMethods.createTypeDocumentBySysName(request.getDoc_type().getType_sys_name(), messageProperties);
            fileAuthContext.requireRefPermissions(request.getFileMetadata(), OperationName.DELETE);
            messageWorkerService.forEachParallel(contextMetadata, deleteFileService::delete);
            return sendResponse(request);
        }
        Optional<ContractType> contractType = contractTypeRepository.findByTypeSysNameOptional(sysNameOfDocument);
        if (contractType.isPresent()) {
            //Удаление файла в контракте
            String path = contractType.get().getEcmDoc()[0].getCode();
            ResponseFromEcmCreateDoc ecmDoc = getEcmObject(request, path);
            if (ecmDoc.getItem() == null) {
                throw new ContractNotFoundException(contractType.get(), request.getEcmId(), request.getAsId());
            }
            RequestFileMetadataContext[] contextMetadata = getContextMetadata(fileIds);
            checkMetadata(new ArrayList<>(fileIds), contextMetadata);
            checkMetadataInDoc(new ArrayList<>(fileIds), ecmDoc);
            setFileType(request, contextMetadata);
            fileAuthContext.requireRefPermissions(request.getFileMetadata(), OperationName.DELETE);
            messageWorkerService.forEachParallel(contextMetadata, deleteFileService::delete);
            return sendResponse(request);
        }

        Optional<DulType> dulType = dulTypeRepository.findByCodeOptional(sysNameOfDocument);
        if (dulType.isPresent()) {
            //Удаление файла из dul
        } else {
            throw new CheckRequiredParametersException(ResponseCodes.REQUIRED_VALUE_MISSING_INT, "Указанный тип документа не существует: " + request.getDocType().getDictValues()[0].getDictValue().getValue());
        }
        return null;
    }

    private void setFileType(DeleteFileRequest request, RequestFileMetadataContext[] contextMetadata) {
        for (DeleteFileRequest.FileMetadataRef fileMetadata : request.getFileMetadata()) {
            for (RequestFileMetadataContext metadataContext : contextMetadata) {
                if (fileMetadata.getEcmId().equals(metadataContext.get__id())) {
                    FileType typ = fileTypeRepository.findById(metadataContext.getFile_type()[0]);
                    PerecoderObject perecoderObject = CommonMethods.createPerecoderObject(PerecoderDictNames.FILETYPES.getName(),
                            PerecoderGroupNames.EADOC.getName(),
                            typ.getFileTypeId());
                    fileMetadata.setFileType(perecoderObject);
                }
            }
        }
    }

    private void checkMetadataInDoc(List<String> requestIdMetadata, ResponseFromEcmCreateDoc ecmDoc) {
        List<String> idMetadataInDoc = ecmDoc.getItem().getFile_metadata().getRows()
                .stream()
                .map(FileMetadataRow::getFileMetadata)
                .collect(Collectors.toList());
        requestIdMetadata.removeAll(idMetadataInDoc);
        if (requestIdMetadata.isEmpty()) {
            return;
        }
        throw new FilesNotFoundException(ecmDoc.getItem().get__id(), requestIdMetadata);
    }

    private ResponseDeleteFileToMq sendResponse(DeleteFileRequest request) {
        String responseMessage = createResponseMessage(request);

        List<FileMetadataDelete> fileMetadataDeleteList = request.getFileMetadata().stream().map(fileMetadata -> FileMetadataDelete.builder().id_ecm_filemetadata(fileMetadata.getEcmId()).id_as_filemetadata(fileMetadata.getAsId()).build()).collect(Collectors.toList());
        FileMetadataDelete[] fileMetadataDeletes;
        if (fileMetadataDeleteList.isEmpty()) {
            fileMetadataDeletes = new FileMetadataDelete[]{};
        } else {
            int size = fileMetadataDeleteList.size();
            fileMetadataDeletes = fileMetadataDeleteList.toArray(new FileMetadataDelete[size]);
        }

        ResponseDeleteFileToMq responseDeleteFileToMq = ResponseDeleteFileToMq.builder()
                .rquid(request.getRequestId())
                .id_ecm_doc(request.getEcmId())
                .id_as_doc(request.getAsId())
                .file_metadata(fileMetadataDeletes)
                .build();
        responseDeleteFileToMq.setResponse_code(ResponseCodes.OK);
        responseDeleteFileToMq.setResponse_message(responseMessage);
        return responseDeleteFileToMq;
    }

    private String createResponseMessage(DeleteFileRequest request) {
        StringBuilder builder = new StringBuilder();
        if (request.getFileMetadata().isEmpty()) {
            return "Запрашиваемых на удаление файлов в документе не обнаружено";
        } else {
            request.getFileMetadata().forEach(fileMetadata -> {
                builder.append("Файл ").append(fileMetadata.getFileName()).append(" (id = ")
                        .append(fileMetadata.getEcmId()).append(") удалён");
                builder.append(System.lineSeparator());
            });
        }
        return builder.toString();
    }

    private void checkMetadata(List<String> requestIdMetadata, RequestFileMetadataContext[] contextMetadata) {
        List<String> contextIdMetadata = Arrays.stream(contextMetadata).map(RequestFileMetadataContext::get__id).collect(Collectors.toList());
        requestIdMetadata.removeAll(contextIdMetadata);
        if (requestIdMetadata.isEmpty()) {
            return;
        }
        throw new FilesNotFoundException(requestIdMetadata);
    }

    private RequestFileMetadataContext[] getContextMetadata(List<String> requestIdMetadata) {
        String body = objectMapperService.getJsonFromObjectRequired(new ElmaListRequest().ids(requestIdMetadata));
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

    private ResponseFromEcmCreateDoc getEcmObject(DeleteFileRequest request, String path) {
        ResponseEntity<String> responseEntity = publicApiElmaService.doPost("",
                ecmProperties.getPathToDocuments(),
                path + SLASH + request.getEcmId(),
                EcmApiConst.GET
        );
        return objectMapperService.getObjectFromJsonRequired(responseEntity.getBody(),
                ResponseFromEcmCreateDoc.class);
    }

}
