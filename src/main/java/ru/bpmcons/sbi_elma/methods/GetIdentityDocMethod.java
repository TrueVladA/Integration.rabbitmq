package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.EcmService;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DulType;
import ru.bpmcons.sbi_elma.ecm.dto.document.IdentityDocument;
import ru.bpmcons.sbi_elma.ecm.dto.reference.Fio;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DulTypeRepository;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.methods.additional.CommonMethods;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.IdentityDoc;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.models.request.GetIdentityDocRequest;
import ru.bpmcons.sbi_elma.properties.*;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

import javax.validation.Valid;

@Service
@Validated
@RequiredArgsConstructor
public class GetIdentityDocMethod {
    private final SysNamesConstants sysNamesConstants;
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;
    private final S3ModuleProperties s3ModuleProperties;
    private final EcmService ecmService;
    private final DulTypeRepository dulTypeRepository;

    @Validated
    @Method("${methods.getidentitydoc}")
    public IdentityDoc doMethod(@Valid GetIdentityDocRequest identityDoc) {
        IdentityDocument doc = ecmService.getDocument(sysNamesConstants.getDul(), identityDoc.getEcmId(), IdentityDocument.class);
        return createSuccessResponseContent(doc);
    }

    private IdentityDoc createSuccessResponseContent(IdentityDocument item) {
        Fio fio = item.getFio();
        IdentityDoc identityDoc = new IdentityDoc();
        identityDoc.setResponse_code(ResponseCodes.OK);
        identityDoc.setResponse_message("success");
        if (item.getIdentityDocType() != null && !item.getIdentityDocType().isBlank()) {
            DulType dulType = dulTypeRepository.findById(item.getIdentityDocType());
//            Optional<CommonSystem> commonSystem = cacheService.searchCommSystemFromEcm(Optional.ofNullable(messageProperties.getAppId()));
            PerecoderObject perecoderObject = CommonMethods.createPerecoderObject(PerecoderDictNames.TYPEIDENTITYDOC.getName(),
                    PerecoderGroupNames.EADOC.getName(),
                    dulType.getCode());
            identityDoc.setCode_identitydoc(perecoderObject);
            identityDoc.setType_identitydoc(dulType.getShortName());
        }
        identityDoc.setApp_id(MessagePropertiesHolder.getMessageProperties().getAppId());
        identityDoc.setFio(fio.getFirstname() + " " + fio.getLastname() + " " + fio.getMiddlename());
        identityDoc.setNumber(item.getNumber());
        identityDoc.setSeries(item.getSeries());
        identityDoc.setIssue_date(item.getIssueDate());
        identityDoc.setEnd_date(item.getEndDate());
        identityDoc.setIssued(item.getIssued());
        identityDoc.setRquid(MessagePropertiesHolder.getMessageProperties().getMessageId());
        identityDoc.setId_as_doc(item.getExternalId());
        identityDoc.setId_ecm_doc(item.getId());
        identityDoc.setFull_number(item.getFullNumber());
        FileMetadata[] fileMetadata = createOrUpdateFileMetadata.createOrUpdateFileMetadataIdentityDoc(item, identityDoc, s3ModuleProperties.getGetMethod(), SecurityContextHolder.getRequiredContext().getSystem(), MessagePropertiesHolder.getRequiredVersion());
        identityDoc.setFile_metadata(fileMetadata);
        return identityDoc;
    }
}
