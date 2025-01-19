package ru.bpmcons.sbi_elma.ecm;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.dict.CommonSystem;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DulType;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.CommonSystemRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DulTypeRepository;
import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListRequest;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.EqFilter;
import ru.bpmcons.sbi_elma.exceptions.IdentityDocNotFoundException;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.methods.CreateOrUpdateFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.doc.Fio;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.IdentityDoc;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.RequestCreateIdentityDoc;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.RequestIdentityDocContext;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.ResponseFromEcmCreateIdentityDoc;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.ResponseFromEcmListIdentityDoc;
import ru.bpmcons.sbi_elma.properties.EcmApiConst;
import ru.bpmcons.sbi_elma.properties.EcmProperties;
import ru.bpmcons.sbi_elma.properties.S3ModuleProperties;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;
import ru.bpmcons.sbi_elma.service.SeparaterFio;

@Service
@RequiredArgsConstructor
public class IdentityDocService {
    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;
    private final ObjectMapperService objectMapperService;
    private final PublicApiElmaService publicApiElmaService;
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;
    private final SeparaterFio separaterFio;
    private final S3ModuleProperties s3ModuleProperties;
    private final CommonSystemRepository commonSystemRepository;
    private final DulTypeRepository dulTypeRepository;


    public void checkIdentityDocById(String id) {
        ResponseEntity<String> response = publicApiElmaService.doPost("",
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getDul() + "/" + id,
                EcmApiConst.GET
        );
        ResponseFromEcmCreateIdentityDoc responseObject = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmCreateIdentityDoc.class);
        if (responseObject.getItem() == null) {
            throw new IdentityDocNotFoundException(id);
        }
    }

    public String[] createIdentityDoc(CommonSystem commonSystem, IdentityDoc identityDoc) {

        RequestIdentityDocContext[] existsIdentityDoc = findExistsIdentityDoc(identityDoc.getFull_number());
        String[] rsl;
        if (existsIdentityDoc.length == 0) {
            String bodyForRequestToEcm = createBodyForRequestToEcmFromAnotherMethod(identityDoc, null, commonSystem, MessagePropertiesHolder.getRequiredVersion());
            ResponseEntity<String> responseObject = publicApiElmaService.doPost(bodyForRequestToEcm,
                    ecmProperties.getPathToDocuments(),
                    sysNamesConstants.getDul(),
                    EcmApiConst.CREATE
            );
            ResponseFromEcmCreateIdentityDoc createdIdentityDoc = objectMapperService.getObjectFromJsonRequired(responseObject.getBody(),
                    ResponseFromEcmCreateIdentityDoc.class);
            rsl = new String[]{createdIdentityDoc.getItem().get__id()};
        } else {
            rsl = new String[]{existsIdentityDoc[0].get__id()};
        }
        return rsl;
    }

    private String createBodyForRequestToEcmFromAnotherMethod(IdentityDoc identityDoc,
                                                              RequestIdentityDocContext existIdentityDoc,
                                                              CommonSystem reqCommonSystem,
                                                              Version reqVersion) {
        RequestCreateIdentityDoc request = new RequestCreateIdentityDoc();
        RequestIdentityDocContext requestContext = new RequestIdentityDocContext();
        Fio fio = separaterFio.separateFio(identityDoc.getFio());
        String app_id = identityDoc.getApp_id();
        if (app_id != null) {
            requestContext.setSource(new String[]{commonSystemRepository.findById(app_id).getId()});
        }
        requestContext.setFio(fio);
        requestContext.setId_as(identityDoc.getId_as_doc());
        requestContext.setIssue_date(identityDoc.getIssue_date());
        requestContext.setEnd_date(identityDoc.getEnd_date());
        requestContext.setIssued(identityDoc.getIssued());
        requestContext.setNumber(identityDoc.getNumber());
        requestContext.setSeries(identityDoc.getSeries());
        if (identityDoc.getFile_metadata() != null && identityDoc.getFile_metadata().length > 0) {
            FileMetadata[] fileMetadataArrayFromContext = createOrUpdateFileMetadata.createOrUpdateFileMetadataIdentityDoc(existIdentityDoc,
                    identityDoc,
                    s3ModuleProperties.getPutMethod(),
                    reqCommonSystem,
                    reqVersion);
            if (fileMetadataArrayFromContext != null && fileMetadataArrayFromContext.length > 0) {
                FileMetadataTable fileMetadataTable = createOrUpdateFileMetadata.createTableFileMetadata(fileMetadataArrayFromContext);
                requestContext.setFile_metadata(fileMetadataTable);
            }
        }
        if (identityDoc.getCode_identitydoc() != null) {
            DulType dulType = dulTypeRepository.findByCode(identityDoc.getCode_identitydoc().getDictValues()[0].getDictValue().getValue());
            requestContext.setType_identitydoc(new String[]{dulType.getId()});
        }
        requestContext.setArchive(false);
        requestContext.setFull_number(identityDoc.getFull_number());
        request.setContext(requestContext);
        return objectMapperService.getJsonFromObjectRequired(request);
    }

    public RequestIdentityDocContext[] findExistsIdentityDoc(String fullNumber) {
        String requestBody = objectMapperService.getJsonFromObjectRequired(
                new ElmaListRequest()
                        .filter(EqFilter.field("doc_full_number", fullNumber)));
        ResponseEntity<String> response = publicApiElmaService.doPost(requestBody,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getDul(),
                EcmApiConst.LIST
        );
        ResponseFromEcmListIdentityDoc responseObject = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmListIdentityDoc.class);
        return responseObject.getResult().getResult();
    }
}
