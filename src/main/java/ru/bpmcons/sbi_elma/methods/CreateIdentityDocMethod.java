package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.IdentityDocService;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DulType;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DulTypeRepository;
import ru.bpmcons.sbi_elma.exceptions.IdentityDocumentAlreadyExistsException;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.models.dto.doc.Fio;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.RequestFileMetadataContext;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.ResponseFromEcmCreateFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.models.dto.generralized.IdentityDoc;
import ru.bpmcons.sbi_elma.models.dto.generralized.Signature;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.RequestCreateIdentityDoc;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.RequestIdentityDocContext;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.ResponseFromEcmCreateIdentityDoc;
import ru.bpmcons.sbi_elma.models.dto.jwt.JwtToken;
import ru.bpmcons.sbi_elma.models.request.CreateIdentityDocRequest;
import ru.bpmcons.sbi_elma.properties.*;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;
import ru.bpmcons.sbi_elma.security.file.extensions.CheckDeniedFileExtensions;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;
import ru.bpmcons.sbi_elma.service.SeparaterFio;

import javax.validation.Valid;

@Service
@Validated
@RequiredArgsConstructor
public class CreateIdentityDocMethod {
    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;
    private final ObjectMapperService objectMapperService;
    private final PublicApiElmaService publicApiElmaService;
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;
    private final SeparaterFio separaterFio;
    private final S3ModuleProperties s3ModuleProperties;
    private final DulTypeRepository dulTypeRepository;
    private final IdentityDocService identityDocService;
    private final MessageWorkerService workerService;

    @Validated
    @CheckDeniedFileExtensions
    @Method("${methods.createidentitydoc}")
    public Object doMethod(@Valid CreateIdentityDocRequest request) {
//        String methodName = methodsName.getCreateDoc();
//        boolean checkRole = permissionChecker.checkMetadata(identityDoc, methodName, messageProperties);
//        if (!checkRole) {
//            return;
//        }
        RequestIdentityDocContext[] existsIdentityDoc = identityDocService.findExistsIdentityDoc(request.getFullNumber());
        if (existsIdentityDoc.length == 0) {
            String bodyForRequestToEcm = getBody(request);
            ResponseEntity<String> responseObject = publicApiElmaService.doPost(bodyForRequestToEcm,
                    ecmProperties.getPathToDocuments(),
                    sysNamesConstants.getDul(),
                    EcmApiConst.CREATE
            );
            return createSuccessResponseToMq(responseObject);
        } else {
            throw new IdentityDocumentAlreadyExistsException(request);
        }
    }

    private Object createSuccessResponseToMq(ResponseEntity<String> responseObject) {
        String body = responseObject.getBody();
        ResponseFromEcmCreateIdentityDoc response = objectMapperService.getObjectFromJsonRequired(body,
                ResponseFromEcmCreateIdentityDoc.class);
        return createSuccessResponseContent(response);
    }

    private Object createSuccessResponseContent(ResponseFromEcmCreateIdentityDoc response) {
        GeneralizedDoc content = new GeneralizedDoc();
        content.setResponse_code(ResponseCodes.OK);
        content.setResponse_message("success");
        RequestIdentityDocContext item = response.getItem();
        content.setId_as_doc(item.getId_as());
        content.setId_ecm_doc(item.get__id());
        MessageProperties messageProperties = MessagePropertiesHolder.getMessageProperties();
        content.setRquid(messageProperties.getMessageId());
        workerService.runInWorker(() -> content.setFile_metadata(
                item.getFile_metadata()
                        .getRows()
                        .stream()
                        .parallel()
                        .map(fileMetadataRow -> getFileMetadata(fileMetadataRow.getFileMetadata()))
                        .map(resp -> createResponseFileMetadata(resp, messageProperties))
                        .toArray(FileMetadata[]::new)
        ));
        return content;
    }

    private ResponseFromEcmCreateFileMetadata getFileMetadata(String idFileMetadata) {
        ResponseEntity<String> stringResponseEntity = publicApiElmaService.doPost("",
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getFileMetadata() + "/" + idFileMetadata,
                EcmApiConst.GET
        );
        return objectMapperService.getObjectFromJsonRequired(stringResponseEntity.getBody(),
                ResponseFromEcmCreateFileMetadata.class);
    }

    private FileMetadata createResponseFileMetadata(ResponseFromEcmCreateFileMetadata fileMetadata, MessageProperties messageProperties) {
        FileMetadata responseFileMetadata = new FileMetadata();
        RequestFileMetadataContext item = fileMetadata.getItem();
        responseFileMetadata.setApp_id(messageProperties.getMessageId());
        responseFileMetadata.setFile_name(item.getFile_name());
        responseFileMetadata.setCrc(item.getCrc());
        responseFileMetadata.setId_as_filemetadata(item.getId_as());
        responseFileMetadata.setId_ecm_filemetadata(item.get__id());
        responseFileMetadata.setUrl_file(item.getUrl_file());
        responseFileMetadata.setUrl_as(item.getUrl_as());
        responseFileMetadata.setEsign(new Signature());
        return responseFileMetadata;
    }

    private IdentityDoc createIdentityDoc(CreateIdentityDocRequest in) {
        IdentityDoc identityDoc = new IdentityDoc();
        identityDoc.setFile_metadata(in.getFileMetadata().toArray(FileMetadata[]::new));
        JwtToken jwtToken = new JwtToken();
        jwtToken.setAccess_token(in.getJwtToken().getAccessToken());
        identityDoc.setJwt_token(jwtToken);
        return identityDoc;
    }

    private String getBody(CreateIdentityDocRequest in) {
        RequestCreateIdentityDoc request = new RequestCreateIdentityDoc();
        RequestIdentityDocContext requestContext = new RequestIdentityDocContext();
        Fio fio = separaterFio.separateFio(in.getFio());
        requestContext.setSource(new String[]{SecurityContextHolder.getRequiredContext().getSystem().getId()});
        requestContext.setFio(fio);
        requestContext.setId_as(in.getAsId());
        requestContext.setIssue_date(in.getIssueDate());
        requestContext.setEnd_date(in.getEndDate());
        requestContext.setIssued(in.getIssued());
        requestContext.setNumber(in.getNumber());
        requestContext.setSeries(in.getSeries());
        IdentityDoc identityDoc = createIdentityDoc(in);
        FileMetadata[] fileMetadataArrayFromContext = createOrUpdateFileMetadata.createOrUpdateFileMetadataIdentityDoc(null, identityDoc, s3ModuleProperties.getPutMethod(), SecurityContextHolder.getRequiredContext().getSystem(), MessagePropertiesHolder.getRequiredVersion());
        if (fileMetadataArrayFromContext != null && fileMetadataArrayFromContext.length > 0) {
            FileMetadataTable fileMetadataTable = createOrUpdateFileMetadata.createTableFileMetadata(fileMetadataArrayFromContext);
            requestContext.setFile_metadata(fileMetadataTable);
        }
        DulType dulType = dulTypeRepository.findByCode(in.getCode().getDictValues()[0].getDictValue().getValue());
        requestContext.setType_identitydoc(new String[]{dulType.getId()});
        requestContext.setArchive(false);
        requestContext.setFull_number(in.getFullNumber());
        request.setContext(requestContext);
        return objectMapperService.getJsonFromObjectRequired(request);
    }
}
