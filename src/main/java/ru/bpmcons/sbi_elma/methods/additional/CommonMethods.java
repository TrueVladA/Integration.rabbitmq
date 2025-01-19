package ru.bpmcons.sbi_elma.methods.additional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.EcmService;
import ru.bpmcons.sbi_elma.ecm.dto.base.EntityBase;
import ru.bpmcons.sbi_elma.ecm.dto.dict.*;
import ru.bpmcons.sbi_elma.ecm.dto.reference.EcmDocumentRef;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.*;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.methods.CreateOrUpdateFileMetadata;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;
import ru.bpmcons.sbi_elma.models.dto.contract.ParentDoc;
import ru.bpmcons.sbi_elma.models.dto.contract.ResponseFromEcmCreateContract;
import ru.bpmcons.sbi_elma.models.dto.creator.Item;
import ru.bpmcons.sbi_elma.models.dto.creator.ResponseFromEcmCreateCreatorEditor;
import ru.bpmcons.sbi_elma.models.dto.doc.DocParties;
import ru.bpmcons.sbi_elma.models.dto.doc.DocPartiesRow;
import ru.bpmcons.sbi_elma.models.dto.doc.Fio;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromPublicApi;
import ru.bpmcons.sbi_elma.models.dto.generralized.*;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.RequestIdentityDocContext;
import ru.bpmcons.sbi_elma.models.dto.identityDoc.ResponseFromEcmCreateIdentityDoc;
import ru.bpmcons.sbi_elma.properties.*;
import ru.bpmcons.sbi_elma.s3.EmptyS3Metadata;
import ru.bpmcons.sbi_elma.s3.S3FileMetadata;
import ru.bpmcons.sbi_elma.s3.S3IdentityDocFileMetadata;
import ru.bpmcons.sbi_elma.s3.S3Metadata;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;
import ru.bpmcons.sbi_elma.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс содержит общие для всех методы для выполнения crud операции документов / метаданных
 */
@Service
@RequiredArgsConstructor
public class CommonMethods {
    Logger logger = LoggerFactory.getLogger(CommonMethods.class);
    private final String SLASH = "/";

    private final ObjectMapperService objectMapperService;
    private final PublicApiElmaService publicApiElmaService;
    private final EcmProperties ecmProperties;
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;
    private final S3ModuleProperties s3ModuleProperties;
    private final SysNamesConstants sysNamesConstants;
    private final VipRepository vipRepository;
    private final OpfRepository opfRepository;
    private final PartyRoleRepository partyRoleRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final PartyTypeRepository partyTypeRepository;
    private final DulTypeRepository dulTypeRepository;
    private final FileTypeRepository fileTypeRepository;
    private final DocTypeRepository docTypeRepository;
    private final CommonSystemRepository commonSystemRepository;
    private final EcmService ecmService;
    private final MessageWorkerService messageWorkerService;

    public ResponseEntity<String> getExistsDoc(Identifiable generalizedDoc) {
        logger.debug("--------------Выполняется поиск существующих документов--------------");
        EcmDocumentRef requestAndType = ecmService.findRef(generalizedDoc);
        return publicApiElmaService.doPost("",
                ecmProperties.getPathToDocuments(),
                requestAndType.getCode() + SLASH + generalizedDoc.getEcmId(),
                EcmApiConst.GET
        );
    }

    /**
     * Метод возвращает json ответ на методы createDoc и updateDoc. В стриме мы сравниваем id_ecm всех метаданных в документе с
     * id_ecm присланных метаданных. Этот фильтр позволяет создавать блокировку только запршашиваемых метаданных,
     * создавать ответ только с запрашиваемыми метаданными. Например, в доке метаданные 1, 2, 3. Прислали запрос на update
     * метаданных 2 и 3. Блокировка на 1 не создастся
     *
     * @param generalizedDoc      входящая обобщёная схема
     * @param response            ответ от ecm на метод update
     * @param requestFileMetadata
     * @return возвращает json для ответа на запрос updateDoc
     */
    public Object createSuccessResponseToMq(GeneralizedDoc generalizedDoc,
                                            EntityBase response,
                                            List<FileMetadata> requestFileMetadata,
                                            OperationName operationName) {
        GeneralizedDoc content = new GeneralizedDoc();
        content.setResponse_code(ResponseCodes.OK);
        content.setResponse_message("success");
        Optional<CommonSystem> commonSystem = commonSystemRepository.findByIdOptional(generalizedDoc.getApp_id());
        commonSystem.ifPresent(system -> content.setApp_sysname(system.getAppSysName()));
        content.setId_as_doc(response.getExternalId());
        content.setId_ecm_doc(response.getId());
        generalizedDoc.setId_ecm_doc(response.getId());
        content.setRquid(MessagePropertiesHolder.getMessageProperties().getMessageId());
        List<FileMetadata> fileMetadataList = new CopyOnWriteArrayList<>();
        MessageProperties properties = MessagePropertiesHolder.getMessageProperties();
        boolean shouldUseMeta = MessagePropertiesHolder.checkVersion(version -> version.isNotBefore(new Version(1, 1, 19)));
        boolean shouldUseCrc = MessagePropertiesHolder.checkVersion(version -> version.isNotBefore(new Version(1, 1, 20)));
        messageWorkerService.runInWorker(() -> Arrays.stream(generalizedDoc.getFile_metadata()).parallel().filter(docFileMetadata ->
                        requestFileMetadata.stream().parallel().anyMatch(inputFileMetadata -> inputFileMetadata.getId_ecm_filemetadata().equals(docFileMetadata.getId_ecm_filemetadata())))
                .forEach(s -> fileMetadataList.add(getFileMetadata(s, properties, generalizedDoc, operationName, shouldUseMeta, shouldUseCrc))));
        FileMetadata[] responseFileMetadata;
        if (fileMetadataList.isEmpty()) {
            responseFileMetadata = new FileMetadata[]{};
        } else {
            int size = fileMetadataList.size();
            responseFileMetadata = new FileMetadata[size];
            fileMetadataList.toArray(responseFileMetadata);
        }
        logger.debug("---------Кол-во метаданных в ответе: " + responseFileMetadata.length);
        content.setFile_metadata(responseFileMetadata);
        return content;
    }

    private FileMetadata getFileMetadata(FileMetadata item, MessageProperties messageProperties, GeneralizedDoc generalizedDoc, OperationName operationName, boolean shouldUseMeta, boolean shouldUseCrc) {
        FileMetadata responseFileMetadata = new FileMetadata();
        responseFileMetadata.setApp_id(messageProperties.getAppId());
        responseFileMetadata.setFile_name(item.getFile_name());
        responseFileMetadata.setCrc(item.getCrc());
        responseFileMetadata.setId_as_filemetadata(item.getId_as_filemetadata());
        responseFileMetadata.setId_ecm_filemetadata(item.getId_ecm_filemetadata());
        DocType docType = (generalizedDoc.getDocType() != null && generalizedDoc.getDocType().valid()) ? docTypeRepository.findBySysName(generalizedDoc.getDocType()) : null;
        ContractType contractType = (generalizedDoc.getContractType() != null && generalizedDoc.getContractType().valid()) ? contractTypeRepository.findByTypeSysName(generalizedDoc.getContractType()) : null;
        FileType ft = fileTypeRepository.findByFileTypeId(item.getFile_type().getSingleValue());
        S3Metadata s3Metadata = shouldUseMeta ? new S3FileMetadata(ft, docType, contractType, operationName, generalizedDoc.getEcmId(), item.getId_ecm_filemetadata()) : new EmptyS3Metadata();
        String presignS3 = createOrUpdateFileMetadata.putPresignUrl(item.getUrl_file(),
                item.isArchive(),
                true,
                shouldUseCrc ? item.getCrc() : null,
                s3Metadata);
        responseFileMetadata.setToken_file(presignS3);
        if (item.getUrl_preview() != null) {
            String presignUrlPreview = createOrUpdateFileMetadata.putPresignUrl(item.getUrl_preview(),
                    item.isArchive(),
                    true,
                    null,
                    s3Metadata);
            responseFileMetadata.setToken_preview(presignUrlPreview);
        }
        responseFileMetadata.setUrl_file(item.getUrl_file());
        responseFileMetadata.setUrl_preview(item.getUrl_preview());
        responseFileMetadata.setUrl_as(item.getUrl_as());
        responseFileMetadata.setEsign(new Signature());
        responseFileMetadata.setProject(item.getProject());
        Optional<FileType> fileType = fileTypeRepository.findByFileTypeIdOptional(item.getFile_type().getDictValues()[0].getDictValue().getValue());
        fileType.ifPresent(type -> {
//            Optional<CommonSystem> commonSystem = cacheService.searchCommSystemFromEcm(Optional.ofNullable(messageProperties.getAppId()));
            PerecoderObject perecoderObject = createPerecoderObject(PerecoderDictNames.FILETYPES.getName(), PerecoderGroupNames.EADOC.getName(), type.getFileTypeId());
            responseFileMetadata.setFile_type(perecoderObject);
        });
        if (item.getCategories() != null) {
            responseFileMetadata.setCategories(Utils.getStringFromMarkdown(item.getCategories()));
        }
        responseFileMetadata.setCreate_date(item.getCreate_date());
        responseFileMetadata.setUpdate_date(item.getUpdate_date());
        responseFileMetadata.setVersion_number(item.getVersion_number());
        responseFileMetadata.setCurrent_version(item.isCurrent_version());
        responseFileMetadata.setMedical_doc(item.isMedical_doc());
        responseFileMetadata.setDoc_size(item.getDoc_size());
        return responseFileMetadata;
    }

    public ResponseEntity<String> findExistParentDoc(ParentDoc generalizedDoc) {
//        logger.info("--------------Выполняется поиск существующих документов--------------");
        String requestBody = "";
        ResponseEntity<String> response = publicApiElmaService.doPost(requestBody,
                ecmProperties.getPathToDocuments(),
                generalizedDoc.getCode() + SLASH + generalizedDoc.getId(),
                EcmApiConst.GET
        );
//        ResponseFromEcmCreateContract responseObject = objectMapperService.getObjectFromJson(response.getBody(),
//                ResponseFromEcmCreateContract.class,
//                messageProperties);
//        logger.info("Найденные документы: " + responseObject.toString());
        return response;
    }

    public InputParentDoc createParentDoc(ParentDoc parent_doc) {
        InputParentDoc inputParentDoc = new InputParentDoc();
        ResponseEntity<String> response = findExistParentDoc(parent_doc);
        ResponseFromEcmCreateContract responseObject = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmCreateContract.class);
        ResponseFromPublicApi existsDoc = responseObject.getItem();
        if (existsDoc != null) {
            inputParentDoc.setId_ecm_doc(existsDoc.get__id());
            inputParentDoc.setId_as_doc(existsDoc.getId_as());
            if (existsDoc.getDoc_type() != null && existsDoc.getDoc_type().length > 0) {
                inputParentDoc.setDoc_type(createPerecoderObjectDocumentById(existsDoc.getDoc_type()[0]));
            } else {
                inputParentDoc.setContract_type(createPerecoderObjectContractById(existsDoc.getContract_type()[0]));
            }
            inputParentDoc.setNumber(existsDoc.getDoc_number());
            inputParentDoc.setSeries(existsDoc.getDoc_series());
            inputParentDoc.setApp_id(existsDoc.getSource()[0]);
        }
        return inputParentDoc;
    }

    public PerecoderObject createPerecoderObjectDocumentById(String docTypeId) {
        PerecoderObject perecoderObject = new PerecoderObject();
        perecoderObject.setDictName(PerecoderDictNames.DOCTYPES.getName());
        DictValue dictValue = new DictValue();
        dictValue.setGroup(PerecoderGroupNames.EADOC.getName());
        DocType docType = docTypeRepository.findById(docTypeId);
        dictValue.setValue(docType.getSysName());
        DictValues dictValues = new DictValues();
        dictValues.setDictValue(dictValue);
        perecoderObject.setDictValues(new DictValues[]{dictValues});
        return perecoderObject;
    }

    public PerecoderObject createPerecoderObjectContractById(String contractTypeId) {
        PerecoderObject perecoderObject = new PerecoderObject();
        perecoderObject.setDictName(PerecoderDictNames.DOCTYPES.getName());
        DictValue dictValue = new DictValue();
        dictValue.setGroup(PerecoderGroupNames.EADOC.getName());
        dictValue.setValue(contractTypeRepository.findById(contractTypeId).getTypeSysName());
        DictValues dictValues = new DictValues();
        dictValues.setDictValue(dictValue);
        perecoderObject.setDictValues(new DictValues[]{dictValues});
        return perecoderObject;
    }

    public DocParty[] createDocParty(DocParties doc_parties,
                                      GeneralizedDoc generalizedDoc,
                                      MessageProperties messageProperties) {
        DocPartiesRow[] rows = doc_parties.getRows();
        if (rows == null) {
            return new DocParty[]{new DocParty()};
        }
        DocParty[] docParty = new DocParty[rows.length];
        for (int i = 0; i < rows.length; i++) {
            DocPartiesRow row = rows[i];
            DocParty party = new DocParty();
            party.setId_as_party(row.getId_as());
            party.setFullname(row.getFull_name());
            if (row.getParty_type() != null) {
                String partyTypeId = row.getParty_type()[0];
                Optional<PartyType> partyType = partyTypeRepository.findByIdOptional(partyTypeId);
                partyType.ifPresent(type -> {
                    party.setType(createPerecoderObject(PerecoderDictNames.PARTYTYPES.getName(), PerecoderGroupNames.EADOC.getName(), partyType.get().getSysName()));
                });
            }
            if (row.getParty_role() != null) {
                String roleId = row.getParty_role()[0];
//                String acSysName = perecoder.getAcSysName(partyRole.get().getRole_sys_name());
//                Optional<CommonSystem> commonSystem = cacheService.searchCommSystemFromEcm(Optional.ofNullable(messageProperties.getAppId()));
                PerecoderObject perecoderObject = createPerecoderObject(PerecoderDictNames.ROLETYPES.getName(), PerecoderGroupNames.EADOC.getName(), partyRoleRepository.findById(roleId).getSysName());
                party.setRole(perecoderObject);
            }
            Fio fio = row.getFio();
            if (fio != null) {
                party.setFio(fio.getFirstname() + " " + fio.getLastname() + " " + fio.getMiddlename());
            }

//            party.setApp_id(messageProperties.getAppId());
            Optional<CommonSystem> commonSystem = commonSystemRepository.findByIdOptional(messageProperties.getAppId());
            commonSystem.ifPresent(system -> party.setApp_sysname(system.getAppSysName()));
            party.setINN(row.getInn());
            party.setBirthdate(row.getBirthdate());
            if (row.getOpf() != null && row.getOpf().length > 0) {
                party.setOpf(findOpf(row.getOpf()[0]));
            }
            party.setShortname(row.getShort_name());
            if (row.getVip() != null && row.getVip().length > 0) {
                party.setVIP(findVip(row.getVip()[0]));
            }
            party.setIdentity_doc(row.getDul());
            if (row.getIdentitydoc() != null && row.getIdentitydoc().length > 0) {
                party.setIdentity_doc_obj(getIdentityDoc(row.getIdentitydoc()[0], generalizedDoc, messageProperties));
            }
            docParty[i] = party;
        }
        return docParty;
    }

    private IdentityDoc getIdentityDoc(String identityDocId, GeneralizedDoc generalizedDoc, MessageProperties messageProperties) {
        String requestBody = "";
        ResponseEntity<String> response = publicApiElmaService.doPost(requestBody,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getDul() + SLASH + identityDocId,
                EcmApiConst.GET
        );
        ResponseFromEcmCreateIdentityDoc responseObject = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmCreateIdentityDoc.class);
        return createIdentityDoc(responseObject.getItem(), generalizedDoc, messageProperties);
    }

    private IdentityDoc createIdentityDoc(RequestIdentityDocContext ecmIdentityDoc, GeneralizedDoc generalizedDoc, MessageProperties messageProperties) {
        if (ecmIdentityDoc == null) {
            return new IdentityDoc();
        }
        IdentityDoc identityDoc = new IdentityDoc();
        identityDoc.setId_ecm_doc(ecmIdentityDoc.get__id());
        identityDoc.setId_as_doc(ecmIdentityDoc.getId_as());
        if (ecmIdentityDoc.getType_identitydoc() != null && ecmIdentityDoc.getType_identitydoc().length > 0) {
            DulType dulType = dulTypeRepository.findById(ecmIdentityDoc.getType_identitydoc()[0]);
            identityDoc.setType_identitydoc(dulType.getEcmName());
//            Optional<CommonSystem> commonSystem = cacheService.searchCommSystemFromEcm(Optional.ofNullable(messageProperties.getAppId()));
            PerecoderObject perecoderObject = CommonMethods.createPerecoderObject(PerecoderDictNames.TYPEIDENTITYDOC.getName(),
                    PerecoderGroupNames.EADOC.getName(),
                    dulType.getCode());
            identityDoc.setCode_identitydoc(perecoderObject);
        }
        identityDoc.setRquid(messageProperties.getMessageId());
        identityDoc.setSeries(ecmIdentityDoc.getSeries());
        identityDoc.setNumber(ecmIdentityDoc.getNumber());
        identityDoc.setIssued(ecmIdentityDoc.getIssued());
        identityDoc.setIssue_date(ecmIdentityDoc.getIssue_date());
        identityDoc.setFull_number(ecmIdentityDoc.getFull_number());
        Fio fio = ecmIdentityDoc.getFio();
        identityDoc.setFio(fio.getLastname() + " " + fio.getFirstname() + " " + fio.getMiddlename());
        identityDoc.setApp_id(messageProperties.getAppId());
        identityDoc.setFile_metadata(createOrUpdateFileMetadata.createOrUpdateFileMetadataIdentityDoc(ecmIdentityDoc, identityDoc, s3ModuleProperties.getGetMethod(), SecurityContextHolder.getRequiredContext().getSystem(), MessagePropertiesHolder.getRequiredVersion()));
        boolean shouldUseMeta = MessagePropertiesHolder.checkVersion(version -> version.isNotBefore(new Version(1, 1, 19)));
        Arrays.stream(identityDoc.getFile_metadata()).forEach(file_metadata -> {
            FileType ft = fileTypeRepository.findByFileTypeId(file_metadata.getFile_type().getSingleValue());
            file_metadata.setToken_file(createOrUpdateFileMetadata.getPresignUrl(file_metadata.getUrl_file(),
                    file_metadata.isArchive(),
                    true,
                    shouldUseMeta ? new S3IdentityDocFileMetadata(ft, OperationName.CREATE, identityDoc.getId_ecm_doc(), file_metadata.getId_ecm_filemetadata()) : new EmptyS3Metadata()));
            if (file_metadata.getUrl_preview() != null) {
                file_metadata.setToken_preview(createOrUpdateFileMetadata.getPresignUrl(file_metadata.getUrl_preview(),
                        file_metadata.isArchive(),
                        true,
                        shouldUseMeta ? new S3IdentityDocFileMetadata(ft, OperationName.CREATE, identityDoc.getId_ecm_doc(), file_metadata.getId_ecm_filemetadata()) : new EmptyS3Metadata()));
            }
        });
        return identityDoc;
    }

    private String findVip(String vipId) {
        return vipRepository.findById(vipId).getSysName();
    }

    private String findOpf(String opfId) {
        return opfRepository.findById(opfId).getName();
    }

    public Creator_editor findCreatorEditor(String author_editor_id) {
        String requestBody = "";
        ResponseEntity<String> ecmCreatorEditor = publicApiElmaService.doPost(requestBody,
                ecmProperties.getPathToReferences(),
                sysNamesConstants.getCreatorEditor() + SLASH + author_editor_id,
                EcmApiConst.GET
        );
        ResponseFromEcmCreateCreatorEditor object = objectMapperService.getObjectFromJsonRequired(ecmCreatorEditor.getBody(),
                ResponseFromEcmCreateCreatorEditor.class);
        Item creatorEditor = object.getItem();
        Creator_editor creator_editor = new Creator_editor();
//        creator_editor.setApp_id(messageProperties.getAppId());
        String sourceId = object.getItem().getSource()[0];
        Optional<CommonSystem> commonSystem = commonSystemRepository.findByIdOptional(sourceId);
        commonSystem.ifPresent(system -> creator_editor.setApp_sysname(system.getAppSysName()));
        creator_editor.setFullname(creatorEditor.get__name());
        creator_editor.setId_as_creator(creatorEditor.getId_as_creator());
        creator_editor.setRole(creatorEditor.getRole());
//        creator_editor.setStaff_number(creatorEditor.getStaff_number() != null ? creatorEditor.getStaff_number() : "");
        creator_editor.setEmail(creatorEditor.getEmail()[0].getEmail());
        creator_editor.setId_ecm_creator(creatorEditor.get__id());
        return creator_editor;
    }

    public static void setParams(GeneralizedDoc generalizedDoc, ResponseFromPublicApi existsDoc, CurrencyRepository currencyRepository, DocFlowRepository docFlowRepository) {
        generalizedDoc.setId_ecm_doc(existsDoc.get__id());
        generalizedDoc.setId_as_doc(existsDoc.getId_as());
        generalizedDoc.setDoc_date(existsDoc.getDoc_date());
        generalizedDoc.setDoc_name(existsDoc.get__name());
        generalizedDoc.setDoc_series(existsDoc.getDoc_series());
        generalizedDoc.setDoc_number(existsDoc.getDoc_number());
        generalizedDoc.setDoc_full_number(existsDoc.getDoc_number());
        generalizedDoc.setContract_date(existsDoc.getContract_date());
        generalizedDoc.setContract_start_date(existsDoc.getContract_date());
        generalizedDoc.setContract_number(existsDoc.getContract_number());
        generalizedDoc.setContract_series(existsDoc.getContract_series());
        generalizedDoc.setContract_full_number(existsDoc.getContract_full_number());

        generalizedDoc.setPaymentPurpose(existsDoc.getPaymentPurpose());
        generalizedDoc.setComment(existsDoc.getComment());
        generalizedDoc.setSum(existsDoc.getSum());
        if (existsDoc.getCurrency() != null && existsDoc.getCurrency().length > 0) {
            String code = currencyRepository.findById(existsDoc.getCurrency()[0]).getDigitalCode();
            generalizedDoc.setCurrencyNum(Integer.valueOf(code));
        }

        generalizedDoc.setDamageDks(existsDoc.getDamageDks());
        if (existsDoc.getFlow() != null) {
            DocFlow doc = docFlowRepository.findById(existsDoc.getFlow());
            generalizedDoc.setFlow(doc.getId());
        }
    }

    public static PerecoderObject createPerecoderObject(String dictName, String group, String value) {
        PerecoderObject perecoderObject = new PerecoderObject();
        perecoderObject.setDictName(dictName);
        DictValue dictValue = new DictValue();
        dictValue.setGroup(group.toUpperCase(Locale.ROOT));
        dictValue.setValue(value);
        DictValues dictValues = new DictValues();
        dictValues.setDictValue(dictValue);
        perecoderObject.setDictValues(new DictValues[]{dictValues});
        return perecoderObject;
    }
}
