package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataRow;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.*;
import ru.bpmcons.sbi_elma.exceptions.ContractNotFoundException;
import ru.bpmcons.sbi_elma.exceptions.DocumentNotFoundException;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessagePropertiesHolder;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.methods.additional.CommonMethods;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;
import ru.bpmcons.sbi_elma.models.dto.contract.ResponseFromEcmCreateContract;
import ru.bpmcons.sbi_elma.models.dto.doc.DocParties;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromPublicApi;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.models.request.GetDocRequest;
import ru.bpmcons.sbi_elma.properties.PerecoderDictNames;
import ru.bpmcons.sbi_elma.properties.PerecoderGroupNames;
import ru.bpmcons.sbi_elma.properties.ResponseCodes;
import ru.bpmcons.sbi_elma.s3.EmptyS3Metadata;
import ru.bpmcons.sbi_elma.s3.S3FileMetadata;
import ru.bpmcons.sbi_elma.s3.S3Metadata;
import ru.bpmcons.sbi_elma.security.authorization.Authorized;
import ru.bpmcons.sbi_elma.security.file.authorization.FileAuthorizationService;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class GetDocMethod {
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;
    private final ObjectMapperService objectMapperService;
    private final CommonMethods commonMethods;
    private final CommonSystemRepository commonSystemRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final DocTypeRepository docTypeRepository;
    private final FileAuthorizationService fileAuthorizationService;
    private final CurrencyRepository currencyRepository;
    private final DocFlowRepository docFlowRepository;
    private final FileTypeRepository fileTypeRepository;
    private final MessageWorkerService messageWorkerService;

    @Validated
    @Method("${methods.getdoc}")
    @Authorized(OperationName.READ)
    public GeneralizedDoc doMethod(@Valid GetDocRequest request) {
        ResponseFromPublicApi existsDoc = findExistsDoc(request);
        if (existsDoc != null) {
            if (existsDoc.getFileMetadata() == null) {
                existsDoc.setFileMetadata(new FileMetadataTable());
            }
            if (existsDoc.getFileMetadata().getRows() == null) {
                existsDoc.getFileMetadata().setRows(List.of());
            }

            List<FileMetadataRow> inMeta = existsDoc.getFileMetadata().getRows();
            if (request.getFileIds() != null) {
                HashSet<String> reqIds = new HashSet<>(request.getFileIds());
                inMeta = inMeta.stream()
                        .filter(fileMetadataRow -> reqIds.contains(fileMetadataRow.getFileMetadata()))
                        .collect(Collectors.toList());
            }
            FileAuthorizationService.Context fileAuthCtx = fileAuthorizationService.buildContext(request);
            List<FileMetadata> rows = createOrUpdateFileMetadata.getFileMetadataArray(inMeta)
                    .stream()
                    .filter(metadata -> !request.isHideArchived() || !metadata.isArchive())
                    .filter(metadata -> fileAuthCtx.checkPermission(metadata.getFile_type().getSingleValue(), OperationName.READ))
                    .collect(Collectors.toList());
            return createGeneralizedDoc(rows, existsDoc, MessagePropertiesHolder.getMessageProperties());
        } else {
            if (request.getDocType() == null || !request.getDocType().valid()) {
                throw new ContractNotFoundException(contractTypeRepository.findByTypeSysName(request.getContractType()), request.getEcmId(), request.getAsId());
            } else {
                throw new DocumentNotFoundException(docTypeRepository.findBySysName(request.getDocType()), request.getEcmId(), request.getAsId());
            }
        }
    }

    private GeneralizedDoc createGeneralizedDoc(List<FileMetadata> fileMeta,
                                                ResponseFromPublicApi existsDoc,
                                                MessageProperties messageProperties) {
        GeneralizedDoc generalizedDocNew = new GeneralizedDoc();
        generalizedDocNew.setResponse_code(ResponseCodes.OK);
        generalizedDocNew.setResponse_message("success");
        generalizedDocNew.setFile_metadata(fileMeta.toArray(FileMetadata[]::new));
        if (existsDoc.getParent_doc() != null) {
            generalizedDocNew.setParent_doc(commonMethods.createParentDoc(existsDoc.getParent_doc()));
        }
        commonSystemRepository.findByIdOptional(existsDoc.getSource()[0])
                .ifPresent(commonSystem -> generalizedDocNew.setApp_sysname(commonSystem.getAppSysName()));
        CommonMethods.setParams(generalizedDocNew, existsDoc, currencyRepository, docFlowRepository);
        if (existsDoc.getCreator() != null && existsDoc.getCreator().length > 0) {
            generalizedDocNew.setCreator(commonMethods.findCreatorEditor(existsDoc.getCreator()[0]));
        }
        if (existsDoc.getEditor() != null && existsDoc.getEditor().length > 0) {
            generalizedDocNew.setEditor(commonMethods.findCreatorEditor(existsDoc.getEditor()[0]));
        }
        DocParties doc_parties = existsDoc.getDoc_parties();
        if (doc_parties != null && doc_parties.getRows() != null) {
            generalizedDocNew.setDoc_parties(commonMethods.createDocParty(doc_parties, generalizedDocNew, messageProperties));
        } else {
            if (existsDoc.getContract_parties() != null && existsDoc.getContract_parties().getRows() != null) {
                generalizedDocNew.setDoc_parties(commonMethods.createDocParty(existsDoc.getContract_parties(), generalizedDocNew, messageProperties));
            }
        }
        DocType docType = null;
        String[] doc_type = existsDoc.getDoc_type();
        if (doc_type != null && doc_type.length > 0) {
            generalizedDocNew.setDoc_type(commonMethods.createPerecoderObjectDocumentById(doc_type[0]));
            docType = docTypeRepository.findById(doc_type[0]);
        }
        ContractType contractType = null;
        String[] contract_type = existsDoc.getContract_type();
        if (contract_type != null && contract_type.length > 0) {
            generalizedDocNew.setContract_type(commonMethods.createPerecoderObjectContractById(contract_type[0]));
            contractType = contractTypeRepository.findById(contract_type[0]);
        }
        generalizedDocNew.setStatus(existsDoc.getStatus());
//        generalizedDocNew.setInsurance_product(existsDoc.getProduct_name());
        String product = existsDoc.getProduct_name();
        if (product != null) {
//            Optional<Product> product = cacheService.getProductByProductLine(product);
//            Optional<CommonSystem> commonSystem = cacheService.searchCommSystemFromEcm(Optional.ofNullable(messageProperties.getAppId()));
//            ProductLine productObject = findProductLine(product[0], messageProperties);
            PerecoderObject perecoderObject = CommonMethods.createPerecoderObject(PerecoderDictNames.PRODUCT.getName(),
                    PerecoderGroupNames.EADOC.getName(),
                    product);
//            generalizedDoc.setProduct_line(findProductLine(product_line[0], messageProperties));
            generalizedDocNew.setInsurance_product(perecoderObject);
        }
        generalizedDocNew.setMedical_doc(existsDoc.isMedical_doc());
        DocType finalDocType = docType;
        ContractType finalContractType = contractType;
        boolean shouldUseMeta = MessagePropertiesHolder.checkVersion(version -> version.isNotBefore(new Version(1, 1, 19)));
        messageWorkerService.forEachParallel(fileMeta, fileMetadata -> {
            S3Metadata metadata = shouldUseMeta ? new S3FileMetadata(fileTypeRepository.findByFileTypeId(fileMetadata.getFile_type().getSingleValue()), finalDocType, finalContractType, OperationName.READ, existsDoc.get__id(), fileMetadata.getId_ecm_filemetadata()) : new EmptyS3Metadata();
            fileMetadata.setToken_file(createOrUpdateFileMetadata.getPresignUrl(fileMetadata.getUrl_file(),
                    fileMetadata.isArchive(),
                    false,
                    metadata));
            if (fileMetadata.getUrl_preview() != null) {
                fileMetadata.setToken_preview(createOrUpdateFileMetadata.getPresignUrl(fileMetadata.getUrl_preview(),
                        fileMetadata.isArchive(),
                        false,
                        metadata));
            }
        });
        //        generalizedDoc.setFile_metadata(fileMetadata);
        return generalizedDocNew;
    }

    private ResponseFromPublicApi findExistsDoc(Identifiable generalizedDoc) {
        ResponseEntity<String> response = commonMethods.getExistsDoc(generalizedDoc);
        ResponseFromEcmCreateContract responseObject = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmCreateContract.class);
        return responseObject.getItem();
    }

}
