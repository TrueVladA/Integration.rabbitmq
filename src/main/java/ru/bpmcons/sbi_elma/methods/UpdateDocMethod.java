package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.EcmDocumentResolver;
import ru.bpmcons.sbi_elma.ecm.EcmService;
import ru.bpmcons.sbi_elma.ecm.dto.base.EntityBase;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ContractTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocumentTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMappingService;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.elma.ElmaClient;
import ru.bpmcons.sbi_elma.exceptions.ContractAlreadyExistsException;
import ru.bpmcons.sbi_elma.exceptions.DocumentAlreadyExistsException;
import ru.bpmcons.sbi_elma.exceptions.DocumentNotFoundException;
import ru.bpmcons.sbi_elma.infra.method.AsyncResultHandler;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.methods.additional.CommonMethods;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromPublicApi;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.models.request.UpdateDocRequest;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.security.authorization.Authorized;
import ru.bpmcons.sbi_elma.security.file.extensions.CheckDeniedFileExtensions;
import ru.bpmcons.sbi_elma.service.LockService;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class UpdateDocMethod {
    public static final String SLASH = "/";

    private final LockService lockService;
    private final CommonMethods commonMethods;
    private final SysNamesConstants sysNamesConstants;
    private final EcmService ecmService;
    private final TraitMappingService traitMappingService;
    private final EcmDocumentResolver ecmDocumentResolver;
    private final ElmaClient elmaClient;
    private final ContractTypeRepository contractTypeRepository;
    private final DocTypeRepository docTypeRepository;

    @Validated
    @CheckDeniedFileExtensions
    @Method("${methods.updatedoc}")
    @Authorized(OperationName.UPDATE)
    public void doMethod(@Valid UpdateDocRequest doc, AsyncResultHandler resultHandler) {
        GeneralizedDoc generalizedDoc = doc.toGeneralizedDoc();
        lockService.checkLock(generalizedDoc.getId_ecm_doc());
        List<FileMetadata> requestFileMetadata = Arrays.stream(generalizedDoc.getFile_metadata()).collect(Collectors.toList());
        Set<String> reqMetadataIdAs = Arrays.stream(generalizedDoc.getFile_metadata())
                .map(FileMetadata::getId_as_filemetadata)
                .collect(Collectors.toSet());
        Object obj;
        synchronized (this) {
            EntityBase existsDoc = ecmService.findDocument(generalizedDoc);
            String appSysName;
            if (existsDoc != null) {
                String path;
                if (generalizedDoc.getDoc_type() == null || generalizedDoc.getDoc_type().getDictValues()[0].getDictValue().getValue().equals("")) {
                    appSysName = generalizedDoc.getContract_type().getDictValues()[0].getDictValue().getValue();
                    path = contractTypeRepository.findByTypeSysName(appSysName).getEcmDoc()[0].getCode();
                } else {
                    DocType docType = docTypeRepository.findBySysName(generalizedDoc.getDoc_type());
                    path = docType.getEcmDoc()[0].getCode();
                }

                checkDuplicates(path, generalizedDoc, existsDoc);

                obj = ecmDocumentResolver.newInstance(path);
                obj = traitMappingService.mapRequired(generalizedDoc, obj, existsDoc);
                obj = traitMappingService.mapRest(generalizedDoc, obj, existsDoc);
                obj = elmaClient.updateDocument(path, existsDoc.getId(), obj);
            } else {
                throw new DocumentNotFoundException(docTypeRepository.findBySysName(generalizedDoc.getDoc_type()), generalizedDoc.getEcmId(), generalizedDoc.getId_as_doc());
            }
        }

        resultHandler.success(commonMethods.createSuccessResponseToMq(generalizedDoc, (EntityBase) obj, requestFileMetadata, OperationName.UPDATE));
        lockService.restoreLock(
                (EntityBase) obj,
                Arrays.stream(generalizedDoc.getFile_metadata())
                        .filter(metadata -> reqMetadataIdAs.contains(metadata.getId_as_filemetadata()))
                        .map(FileMetadata::getId_ecm_filemetadata)
                        .collect(Collectors.toList())
        );
    }

    private void checkDuplicates(String path, GeneralizedDoc generalizedDoc, EntityBase existDoc) {
        if (sysNamesConstants.getContract().equals(path)) {
            String contractSysName = generalizedDoc.getContract_type().getSingleValue();
            ContractType reqType = contractTypeRepository.findByTypeSysName(contractSysName);
            String existType = ((ContractTrait) existDoc).getContractType();
            if (reqType.getId().equals(existType)) { // если поле не поменялось - проверку не выполняем
                return;
            }
            // если мы нашли другой контракт с такими же ID-полями, то кидаем ошибку
            List<ResponseFromPublicApi> docs = ecmService.getSameContracts(reqType, generalizedDoc)
                    .stream()
                    .filter(responseFromPublicApi -> !Objects.equals(responseFromPublicApi.get__id(), generalizedDoc.getId_ecm_doc()))
                    .collect(Collectors.toList());
            if (!docs.isEmpty()) {
                throw new ContractAlreadyExistsException(generalizedDoc, docs.get(0).get__id());
            }
        } else {
            if (existDoc instanceof DocumentTrait && generalizedDoc.getDocType() != null) {
                String existType = ((DocumentTrait) existDoc).getDocType();
                DocType reqType = docTypeRepository.findBySysName(generalizedDoc.getDocType());
                if (reqType.getId().equals(existType)) { // если поле не поменялось - проверку не выполняем
                    return;
                }
            }

            // если мы нашли другой документ с такими же ID-полями, то кидаем ошибку
            List<ResponseFromPublicApi> docs = ecmService.getSameDocuments(path, generalizedDoc)
                    .stream()
                    .filter(responseFromPublicApi -> !Objects.equals(responseFromPublicApi.get__id(), generalizedDoc.getId_ecm_doc()))
                    .collect(Collectors.toList());
            if (!docs.isEmpty()) {
                throw new DocumentAlreadyExistsException(generalizedDoc, docs.get(0).get__id());
            }
        }
    }
}
