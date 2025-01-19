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
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMappingService;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.elma.ElmaClient;
import ru.bpmcons.sbi_elma.exceptions.ContractAlreadyExistsException;
import ru.bpmcons.sbi_elma.exceptions.DocumentAlreadyExistsException;
import ru.bpmcons.sbi_elma.exceptions.DocumentCreatingException;
import ru.bpmcons.sbi_elma.exceptions.DocumentTypeNotSpecifiedException;
import ru.bpmcons.sbi_elma.infra.method.AsyncResultHandler;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.methods.additional.CommonMethods;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromPublicApi;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.models.request.CreateDocRequest;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;
import ru.bpmcons.sbi_elma.security.authorization.Authorized;
import ru.bpmcons.sbi_elma.security.file.extensions.CheckDeniedFileExtensions;
import ru.bpmcons.sbi_elma.service.LockService;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class CreateDocMethod {

    private final LockService lockService;
    private final CommonMethods commonMethods;
    private final SysNamesConstants sysNamesConstants;
    private final EcmDocumentResolver ecmDocumentResolver;
    private final TraitMappingService traitMappingService;
    private final EcmService ecmService;
    private final ElmaClient elmaClient;
    private final ContractTypeRepository contractTypeRepository;
    private final DocTypeRepository docTypeRepository;

    private final Set<String> creatingDocuments = new HashSet<>();

    /**
     * Метод создания документа
     */
    @Validated
    @CheckDeniedFileExtensions
    @Method("${methods.createdoc}")
    @Authorized(OperationName.CREATE)
    public void doMethod(@Valid CreateDocRequest docR, AsyncResultHandler resultHandler) {
        GeneralizedDoc doc = docR.toGeneralizedDoc();
        String path;
        if (doc.getDoc_type() != null) {
            DocType docType = docTypeRepository.findBySysName(doc.getDoc_type());
            path = docType.getEcmDoc()[0].getCode();
        } else if (doc.getContract_type() != null) {
            path = sysNamesConstants.getContract();
        } else {
            throw new DocumentTypeNotSpecifiedException();
        }

        String docUniqId = path + "#" + doc.getId_as_doc();
        synchronized (this) {
            if (creatingDocuments.contains(docUniqId)) {
                throw new DocumentCreatingException();
            }
            creatingDocuments.add(docUniqId);
        }

        try {
            List<String> requestedMetadataIds = Arrays.stream(doc.getFile_metadata()).map(FileMetadata::getId_as_filemetadata).collect(Collectors.toList());

            if (path.equals(sysNamesConstants.getContract())) {
                ContractType type = contractTypeRepository.findByTypeSysName(doc.getContract_type());
                List<ResponseFromPublicApi> docs = ecmService.getSameContractsWithIdAs(type, SecurityContextHolder.getRequiredContext().getSystem(), doc);
                if (!docs.isEmpty()) {
                    throw new ContractAlreadyExistsException(doc, docs.get(0).get__id());
                }
            } else {
                List<ResponseFromPublicApi> docs = ecmService.getSameDocumentsWithIdAs(path, SecurityContextHolder.getRequiredContext().getSystem(), doc);
                if (!docs.isEmpty()) {
                    throw new DocumentAlreadyExistsException(doc, docs.get(0).get__id());
                }
            }

            Object obj = ecmDocumentResolver.newInstance(path);
            obj = traitMappingService.mapRequired(doc, obj, null);
            synchronized (this) {
                obj = elmaClient.createDocument(path, obj);
            }
            List<FileMetadata> requestFileMetadata = Arrays.stream(doc.getFile_metadata()).collect(Collectors.toList());
            Object payload = commonMethods.createSuccessResponseToMq(doc, (EntityBase) obj, requestFileMetadata, OperationName.CREATE);
            resultHandler.success(payload);

            obj = traitMappingService.mapRest(doc, obj, null);
            obj = elmaClient.updateDocument(path, ((EntityBase) obj).getId(), obj);

            String[] requestedMetaArray;
            if (requestedMetadataIds.isEmpty()) {
                requestedMetaArray = new String[]{};
            } else {
                requestedMetaArray = new String[requestedMetadataIds.size()];
                requestedMetadataIds.toArray(requestedMetaArray);
            }
            String[] updatedMetadata = getUpdatedMetadata(requestedMetaArray, doc.getFile_metadata());
            lockService.createLock(((EntityBase) obj),
                    updatedMetadata);
        } finally {
            synchronized (this) {
                creatingDocuments.remove(docUniqId);
            }
        }
    }

    private String[] getUpdatedMetadata(String[] metadataInRequest, FileMetadata[] file_metadata) {
        List<String> rsl = new ArrayList<>();
        for (FileMetadata fileMetadata : file_metadata) {
            for (String id : metadataInRequest) {
                if (fileMetadata.getId_as_filemetadata().equals(id)) {
                    rsl.add(fileMetadata.getId_ecm_filemetadata());
                }
            }
        }
        if (rsl.isEmpty()) {
            return new String[]{};
        } else {
            return rsl.toArray(new String[0]);
        }
//        return rsl.toArray(String[]::new);
    }
}
