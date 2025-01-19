package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DulType;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DulTypeRepository;
import ru.bpmcons.sbi_elma.elma.ElmaClient;
import ru.bpmcons.sbi_elma.exceptions.IdentityDocumentNotFoundException;
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
import ru.bpmcons.sbi_elma.models.request.UpdateIdentityDocRequest;
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
public class UpdateIdentityDocMethod {
    public static final String SLASH = "/";
    Logger logger = LoggerFactory.getLogger(UpdateIdentityDocMethod.class);

    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;
    private final ObjectMapperService objectMapperService;
    private final PublicApiElmaService publicApiElmaService;
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;
    private final SeparaterFio separaterFio;
    private final S3ModuleProperties s3ModuleProperties;
    private final DulTypeRepository dulTypeRepository;
    private final ElmaClient elmaClient;
    private final MessageWorkerService messageWorkerService;

    @Validated
    @CheckDeniedFileExtensions
    @Method("${methods.updateidentitydoc}")
    public GeneralizedDoc doMethod(@Valid UpdateIdentityDocRequest identityDoc) {
//        String methodName = methodsName.getCreateDoc();
//        boolean checkRole = permissionChecker.checkMetadata(identityDoc, methodName, messageProperties);
//        if (!checkRole) {
//            return;
//        }
        RequestIdentityDocContext existsIdentityDoc = elmaClient.getDocument(sysNamesConstants.getDul(), identityDoc.getEcmId(), RequestIdentityDocContext.class);
        if (existsIdentityDoc != null) {
            String bodyForRequestToEcm = getBody(identityDoc, existsIdentityDoc);
            ResponseEntity<String> responseObject = publicApiElmaService.doPost(bodyForRequestToEcm,
                    ecmProperties.getPathToDocuments(),
                    sysNamesConstants.getDul() + SLASH + existsIdentityDoc.get__id(),
                    EcmApiConst.UPDATE
            );
            logger.debug(responseObject.getBody());
            return createSuccessResponseToMq(responseObject);
        } else {
            throw new IdentityDocumentNotFoundException(identityDoc);
        }
    }
    private IdentityDoc createIdentityDoc(UpdateIdentityDocRequest in) {
        IdentityDoc identityDoc = new IdentityDoc();
        identityDoc.setFile_metadata(in.getFileMetadata().toArray(FileMetadata[]::new));
        JwtToken jwtToken = new JwtToken();
        jwtToken.setAccess_token(in.getJwtToken().getAccessToken());
        identityDoc.setJwt_token(jwtToken);
        return identityDoc;
    }

    private String getBody(UpdateIdentityDocRequest identityDoc, RequestIdentityDocContext existIdentityDoc) {
        RequestCreateIdentityDoc request = new RequestCreateIdentityDoc();
        RequestIdentityDocContext requestContext = new RequestIdentityDocContext();
        Fio fio = separaterFio.separateFio(identityDoc.getFio());
        requestContext.setSource(new String[]{SecurityContextHolder.getRequiredContext().getSystem().getId()});
        requestContext.setFio(fio);
        requestContext.setId_as(identityDoc.getAsId());
        requestContext.setIssue_date(identityDoc.getIssueDate());
        requestContext.setEnd_date(identityDoc.getEndDate());
        requestContext.setIssued(identityDoc.getIssued());
        requestContext.setNumber(identityDoc.getNumber());
        requestContext.setSeries(identityDoc.getSeries());
        IdentityDoc identityDoc1 = createIdentityDoc(identityDoc);
        FileMetadata[] fileMetadataArrayFromContext = createOrUpdateFileMetadata.createOrUpdateFileMetadataIdentityDoc(existIdentityDoc, identityDoc1, s3ModuleProperties.getPutMethod(), SecurityContextHolder.getRequiredContext().getSystem(), MessagePropertiesHolder.getRequiredVersion());
        if (fileMetadataArrayFromContext != null && fileMetadataArrayFromContext.length > 0) {
            FileMetadataTable fileMetadataTable = createOrUpdateFileMetadata.createTableFileMetadata(fileMetadataArrayFromContext);
            requestContext.setFile_metadata(fileMetadataTable);
        }
        DulType dulType = dulTypeRepository.findByCode(identityDoc.getCode().getDictValues()[0].getDictValue().getValue());
        requestContext.setType_identitydoc(new String[]{dulType.getId()});
        requestContext.setArchive(false);
        requestContext.setFull_number(identityDoc.getFullNumber());
        request.setContext(requestContext);
        return objectMapperService.getJsonFromObjectRequired(request);
    }

    private GeneralizedDoc createSuccessResponseToMq(ResponseEntity<String> responseObject) {
        String body = responseObject.getBody();
        ResponseFromEcmCreateIdentityDoc response = objectMapperService.getObjectFromJsonRequired(body,
                ResponseFromEcmCreateIdentityDoc.class);
        return createSuccessResponseContent(response);
    }

    private GeneralizedDoc createSuccessResponseContent(ResponseFromEcmCreateIdentityDoc response) {
        GeneralizedDoc content = new GeneralizedDoc();
        content.setResponse_code(ResponseCodes.OK);
        content.setResponse_message("success");
        RequestIdentityDocContext item = response.getItem();
        content.setId_as_doc(item.getId_as());
        content.setId_ecm_doc(item.get__id());
        MessageProperties messageProperties = MessagePropertiesHolder.getMessageProperties();
        content.setRquid(messageProperties.getMessageId());
        messageWorkerService.runInWorker(() -> {
            content.setFile_metadata(
                    item.getFile_metadata()
                            .getRows()
                            .stream()
                            .parallel()
                            .map(row -> getFileMetadata(row.getFileMetadata()))
                            .map(row -> createResponseFileMetadata(row, messageProperties))
                            .toArray(FileMetadata[]::new)
            );
        });
        return content;
    }

    private ResponseFromEcmCreateFileMetadata getFileMetadata(String idFileMetadata) {
        ResponseEntity<String> stringResponseEntity = publicApiElmaService.doPost("",
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getFileMetadata() + SLASH + idFileMetadata,
                EcmApiConst.GET
        );
        return objectMapperService.getObjectFromJsonRequired(stringResponseEntity.getBody(),
                ResponseFromEcmCreateFileMetadata.class);
    }

    private FileMetadata createResponseFileMetadata(ResponseFromEcmCreateFileMetadata fileMetadata, MessageProperties messageProperties) {
        FileMetadata responseFileMetadata = new FileMetadata();
        RequestFileMetadataContext item = fileMetadata.getItem();
        responseFileMetadata.setApp_id(messageProperties.getAppId());
        responseFileMetadata.setFile_name(item.getFile_name());
        responseFileMetadata.setCrc(item.getCrc());
        responseFileMetadata.setId_as_filemetadata(item.getId_as());
        responseFileMetadata.setId_ecm_filemetadata(item.get__id());
        responseFileMetadata.setUrl_file(item.getUrl_file());
        responseFileMetadata.setUrl_as(item.getUrl_as());
        responseFileMetadata.setEsign(new Signature());
        return responseFileMetadata;
    }
}
