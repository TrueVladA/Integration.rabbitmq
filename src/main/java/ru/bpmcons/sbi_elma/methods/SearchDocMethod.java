package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.*;
import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListRequest;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.AndFilter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.EqFilter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.Filter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.LinkFilter;
import ru.bpmcons.sbi_elma.exceptions.DocumentTypeNotSpecifiedException;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.methods.additional.CommonMethods;
import ru.bpmcons.sbi_elma.models.dto.doc.DocParties;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromEcmListDoc;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromPublicApi;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.models.dto.responseMq.CodeMessage;
import ru.bpmcons.sbi_elma.models.dto.searchDoc.ResponseSearchDoc;
import ru.bpmcons.sbi_elma.models.request.SearchDocRequest;
import ru.bpmcons.sbi_elma.properties.*;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Validated
@RequiredArgsConstructor
public class SearchDocMethod {
    Logger logger = LoggerFactory.getLogger(UpdateDocMethod.class);

    private final ObjectMapperService objectMapperService;
    private final EcmProperties ecmProperties;
    private final PublicApiElmaService publicApiElmaService;
    private final CommonMethods commonMethods;
    private final CommonSystemRepository commonSystemRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final DocTypeRepository docTypeRepository;
    private final CurrencyRepository currencyRepository;
    private final DocFlowRepository docFlowRepository;
    private final BehaviorProperties behaviorProperties;
    private final MessageWorkerService messageWorkerService;

    @Validated
    @Method("${methods.searchdoc}")
    public CodeMessage doMethod(@Valid SearchDocRequest generalizedDoc) {
        String requestBody = buildReqSearchDoc(generalizedDoc);

        if (requestBody == null) {
            CodeMessage codeMessage = new CodeMessage();
            codeMessage.setResponse_code(ResponseCodes.OK);
            codeMessage.setResponse_message("Документы не найдены");
            codeMessage.setRquid(MessagePropertiesHolder.getMessageProperties().getMessageId());
            return codeMessage;
        }

        String path;
        if (generalizedDoc.getDocType() != null && generalizedDoc.getDocType().valid()) {
            PerecoderObject doc_type = generalizedDoc.getDocType();
            String appSysName = doc_type.getDictValues()[0].getDictValue().getValue();
            DocType docType = docTypeRepository.findBySysName(appSysName);
            path = docType.getEcmDoc()[0].getCode();
        } else {
            PerecoderObject contract_type = generalizedDoc.getContractType();
            if (contract_type == null || !contract_type.valid()) {
                throw new DocumentTypeNotSpecifiedException();
            }
            String appSysName = contract_type.getDictValues()[0].getDictValue().getValue();
            path = contractTypeRepository.findByTypeSysName(appSysName).getEcmDoc()[0].getCode();
        }


        ResponseEntity<String> responseList = publicApiElmaService.doPost(requestBody,
                ecmProperties.getPathToDocuments(),
                path,
                EcmApiConst.LIST
        );
        ResponseFromEcmListDoc objectFromJson = objectMapperService.getObjectFromJsonRequired(responseList.getBody(),
                ResponseFromEcmListDoc.class);
        ResponseFromPublicApi[] listObjects = objectFromJson.getResult().getResult();
        logger.debug(Arrays.toString(listObjects));
        logger.debug(String.valueOf(responseList));
        if (listObjects.length == 0) {
            CodeMessage codeMessage = new CodeMessage();
            codeMessage.setResponse_code(ResponseCodes.OK);
            codeMessage.setResponse_message("Документы не найдены");
            codeMessage.setRquid(MessagePropertiesHolder.getMessageProperties().getMessageId());
            return codeMessage;
        } else {
            return getRequestBody(listObjects);
        }
    }

    private String buildReqSearchDoc(SearchDocRequest generalizedDoc) {
        List<Filter> filters = buildFilters(generalizedDoc);
        if (filters == null) {
            return null;
        }

        filters = new ArrayList<>(filters);
        if (behaviorProperties.isHideArchivedDocs()) {
            filters.add(EqFilter.field("archive", false));
        }
        return objectMapperService.getJsonFromObjectRequired(new ElmaListRequest()
                .allFields()
                .filter(AndFilter.and(filters.toArray(Filter[]::new))));
    }

    @Nullable
    private List<Filter> buildFilters(SearchDocRequest generalizedDoc) {
        String docType = generalizedDoc.getDocType() == null ? null : generalizedDoc.getDocType().getSingleValue();
        String contrType = generalizedDoc.getContractType() == null ? null : generalizedDoc.getContractType().getSingleValue();
        String fullNumberDoc = generalizedDoc.getDocFullNumber();
        String fullNumberContr = generalizedDoc.getContractFullNumber();
        String docSeries = generalizedDoc.getDocSeries();
        String docNumber = generalizedDoc.getDocNumber();

        if (docType != null && fullNumberDoc != null) {
            return List.of(
                    EqFilter.field("doc_full_number", fullNumberDoc),
                    LinkFilter.list("doc_type", docTypeRepository.findBySysName(docType).getId())
            );
        } else if (docType != null && docSeries != null && docNumber != null) {
            return List.of(
                    EqFilter.field("doc_series", docSeries),
                    EqFilter.field("doc_number", docNumber),
                    LinkFilter.list("doc_type", docTypeRepository.findBySysName(docType).getId())
            );
        } else if (docType != null && docNumber != null) {
            return List.of(
                    EqFilter.field("doc_number", docNumber),
                    LinkFilter.list("doc_type", docTypeRepository.findBySysName(docType).getId())
            );
        } else if(fullNumberContr != null && contrType != null){
            return List.of(
                    EqFilter.field("contract_full_number", fullNumberContr),
                    LinkFilter.list("contract_type", contractTypeRepository.findByTypeSysName(contrType).getId())
            );
        } else if(contrType != null && generalizedDoc.getContractNumber() != null && generalizedDoc.getContractSeries() != null){
            return List.of(
                    EqFilter.field("contract_number", generalizedDoc.getContractNumber()),
                    EqFilter.field("contract_series", generalizedDoc.getContractSeries()),
                    LinkFilter.list("contract_type", contractTypeRepository.findByTypeSysName(contrType).getId())
            );
        } else {
            return null;
        }
    }

    private ResponseSearchDoc getRequestBody(ResponseFromPublicApi[] existsDocs) {
        ResponseSearchDoc response = new ResponseSearchDoc();
        response.setResponse_code(ResponseCodes.OK);
        response.setResponse_message("success");
        MessageProperties messageProperties = MessagePropertiesHolder.getMessageProperties();
        response.setRquid(messageProperties.getMessageId());
        CopyOnWriteArrayList<GeneralizedDoc> list = new CopyOnWriteArrayList<GeneralizedDoc>();
        messageWorkerService.forEachParallel(existsDocs, existDoc -> {
            GeneralizedDoc createdObject = createGeneralizedDoc(existDoc, messageProperties);
            list.add(createdObject);
        });
        if (list.isEmpty()) {
            response.setDocuments(new GeneralizedDoc[]{});
        } else {
            int size = list.size();
            response.setDocuments(list.toArray(new GeneralizedDoc[size]));
        }
        return response;
    }

    private GeneralizedDoc createGeneralizedDoc(ResponseFromPublicApi existsDoc,
                                                MessageProperties messageProperties) {
        GeneralizedDoc generalizedDoc = new GeneralizedDoc();
        if (existsDoc.getParent_doc() != null) {
            generalizedDoc.setParent_doc(commonMethods.createParentDoc(existsDoc.getParent_doc()));
        }
        if (existsDoc.getSource() != null && existsDoc.getSource().length > 0) {
            commonSystemRepository.findByIdOptional(existsDoc.getSource()[0])
                    .ifPresent(commonSystem -> generalizedDoc.setApp_sysname(commonSystem.getAppSysName()));
        }
        CommonMethods.setParams(generalizedDoc, existsDoc, currencyRepository, docFlowRepository);
        if (existsDoc.getCreator() != null && existsDoc.getCreator().length > 0) {
            generalizedDoc.setCreator(commonMethods.findCreatorEditor(existsDoc.getCreator()[0]));
        }
        if (existsDoc.getEditor() != null && existsDoc.getEditor().length > 0) {
            generalizedDoc.setEditor(commonMethods.findCreatorEditor(existsDoc.getEditor()[0]));
        }
        DocParties doc_parties = existsDoc.getDoc_parties();
        if (doc_parties != null && doc_parties.getRows() != null) {
            generalizedDoc.setDoc_parties(commonMethods.createDocParty(doc_parties, generalizedDoc, messageProperties));
        } else {
            if (existsDoc.getContract_parties() != null && existsDoc.getContract_parties().getRows() != null) {
                generalizedDoc.setDoc_parties(commonMethods.createDocParty(existsDoc.getContract_parties(), generalizedDoc, messageProperties));
            }
        }
        String[] doc_type = existsDoc.getDoc_type();
        if (doc_type != null && doc_type.length > 0) {
            generalizedDoc.setDoc_type(commonMethods.createPerecoderObjectDocumentById(doc_type[0]));
        }
        String[] contract_type = existsDoc.getContract_type();
        if (contract_type != null && contract_type.length > 0) {
            generalizedDoc.setContract_type(commonMethods.createPerecoderObjectContractById(contract_type[0]));
        }
        generalizedDoc.setStatus(existsDoc.getStatus());
//        generalizedDoc.setInsurance_product(existsDoc.getProduct_name());
        String product = existsDoc.getProduct_name();
        if (product != null) {
            PerecoderObject perecoderObject = CommonMethods.createPerecoderObject(PerecoderDictNames.PRODUCT.getName(),
                    PerecoderGroupNames.EADOC.getName(),
                    product);
            generalizedDoc.setInsurance_product(perecoderObject);
        }
        generalizedDoc.setMedical_doc(existsDoc.isMedical_doc());
//        FileMetadata[] fileMetadata = createOrUpdateFileMetadata.getFileMetadataArray(existsDoc.getFile_metadata().getRows(), messageProperties); //TODO не отдаём файлы
//        FileMetadata[] fileMetadata = createOrUpdateFileMetadata.createOrUpdateFileMetadata(existsDoc,
//                generalizedDoc,
//                s3ModuleProperties.getGetMethod(),
//                messageProperties);
//        Arrays.stream(fileMetadata).forEach(file_metadata -> {
//            file_metadata.setToken_file(createOrUpdateFileMetadata.getPresignUrl(file_metadata.getUrl_file(),
//                    s3ModuleProperties.getGetMethod(),
//                    messageProperties));
//            if (file_metadata.getUrl_preview() != null) {
//                file_metadata.setToken_preview(createOrUpdateFileMetadata.getPresignUrl(file_metadata.getUrl_preview(),
//                        s3ModuleProperties.getGetMethod(),
//                        messageProperties));
//            }
//        });
//        generalizedDoc.setFile_metadata(fileMetadata);
        return generalizedDoc;
    }

}
