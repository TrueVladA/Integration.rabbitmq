package ru.bpmcons.sbi_elma.ecm;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;
import ru.bpmcons.sbi_elma.ecm.dto.base.EntityBase;
import ru.bpmcons.sbi_elma.ecm.dto.dict.CommonSystem;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataStatus;
import ru.bpmcons.sbi_elma.ecm.dto.reference.CreatorEditor;
import ru.bpmcons.sbi_elma.ecm.dto.reference.EcmDocumentRef;
import ru.bpmcons.sbi_elma.ecm.dto.reference.Role;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.elma.ElmaClient;
import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListRequest;
import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListResponse;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.AndFilter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.EqFilter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.InFilter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.LinkFilter;
import ru.bpmcons.sbi_elma.models.dto.doc.ResponseFromPublicApi;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.RequestFileMetadataContext;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;
import ru.bpmcons.sbi_elma.properties.BehaviorProperties;
import ru.bpmcons.sbi_elma.properties.EcmProperties;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EcmService {
    private final ElmaClient elmaClient;
    private final EcmDocumentResolver ecmDocumentResolver;
    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;
    private final DocTypeRepository docTypeRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final BehaviorProperties behaviorProperties;

    /**
     * Ищет такие же контракты перед созданием/изменением
     */
    public List<ResponseFromPublicApi> getSameContractsWithIdAs(ContractType contract, CommonSystem system, GeneralizedDoc doc) {
        List<Future<List<ResponseFromPublicApi>>> tasks = new ArrayList<>();
        if (doc.getId_as_doc() != null) {
            tasks.add(ForkJoinPool.commonPool().submit(() -> elmaClient.list(
                    ecmProperties.getPathToDocuments(),
                    sysNamesConstants.getContract(),
                    ResponseFromPublicApi.class,
                    new ElmaListRequest().filter(AndFilter.and(
                            LinkFilter.list("source", system.getId()),
                            LinkFilter.list("contract_type", contract.getId()),
                            EqFilter.field("id_as", doc.getId_as_doc()),
                            behaviorProperties.isHideArchivedDocs() ? EqFilter.field("archive", false) : null
                    ).optimize())
            ).getResult()));
        }
        if (doc.getContract_full_number() != null) {
            tasks.add(ForkJoinPool.commonPool().submit(() -> getSameContracts(contract, doc)));
        }
        return tasks.stream()
                .flatMap(listFuture -> {
                    try {
                        return listFuture.get().stream();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Ищет такие же контракты перед созданием/изменением
     */
    public List<ResponseFromPublicApi> getSameContracts(ContractType contract, GeneralizedDoc doc) {
        return elmaClient.list(
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getContract(),
                ResponseFromPublicApi.class,
                new ElmaListRequest().filter(AndFilter.and(
                        LinkFilter.list("contract_type", contract.getId()),
                        EqFilter.field("contract_full_number", doc.getContract_full_number()),
                        behaviorProperties.isHideArchivedDocs() ? EqFilter.field("archive", false) : null
                ).optimize())
        ).getResult();
    }

    /**
     * Ищет такие же документы перед созданием/изменением
     */
    public List<ResponseFromPublicApi> getSameDocumentsWithIdAs(String docType, CommonSystem system, GeneralizedDoc doc) {
        List<Future<List<ResponseFromPublicApi>>> tasks = new ArrayList<>();
        if (doc.getId_as_doc() != null) {
            tasks.add(ForkJoinPool.commonPool().submit(() -> elmaClient.list(
                    ecmProperties.getPathToDocuments(),
                    docType,
                    ResponseFromPublicApi.class,
                    new ElmaListRequest().filter(AndFilter.and(
                            LinkFilter.list("source", system.getId()),
                            EqFilter.field("id_as", doc.getId_as_doc()),
                            behaviorProperties.isHideArchivedDocs() ? EqFilter.field("archive", false) : null
                    ).optimize())
            ).getResult()));
        }
        if (doc.getDoc_full_number() != null) {
            tasks.add(ForkJoinPool.commonPool().submit(() -> getSameDocuments(docType, doc)));
        }
        return tasks.stream()
                .flatMap(listFuture -> {
                    try {
                        return listFuture.get().stream();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }


    /**
     * Ищет такие же документы перед созданием/изменением
     */
    public List<ResponseFromPublicApi> getSameDocuments(String docType, GeneralizedDoc doc) {
        return elmaClient.list(
                ecmProperties.getPathToDocuments(),
                docType,
                ResponseFromPublicApi.class,
                new ElmaListRequest().filter(
                        AndFilter.and(
                                EqFilter.field("doc_full_number", doc.getDoc_full_number()),
                                behaviorProperties.isHideArchivedDocs() ? EqFilter.field("archive", false) : null
                        ).optimize()
                )
        ).getResult();
    }

    public EcmDocumentRef findRef(Identifiable identifiable) {
        if (identifiable.getDocType() == null || !identifiable.getDocType().valid()) {
            ContractType contractType = contractTypeRepository.findByTypeSysName(identifiable.getContractType());
            return contractType.getEcmDoc()[0];
        } else {
            DocType docType = docTypeRepository.findBySysName(identifiable.getDocType());
            return docType.getEcmDoc()[0];
        }
    }

    @Nullable
    public <T extends EntityBase> T findDocument(Identifiable identifiable) {
        return findDocument(findRef(identifiable), identifiable.getEcmId());
    }

    @Nullable
    public <T extends EntityBase> T findDocument(EcmDocumentRef doc, String id) {
        //noinspection unchecked
        return (T) elmaClient.getDocument(doc.getCode(), id, ecmDocumentResolver.getClass(doc.getCode()));
    }

    @NonNull
    public Map<String, RequestFileMetadataContext> getFileMetadataList(@NonNull List<String> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }

        return elmaClient.list(
                        ecmProperties.getPathToDocuments(),
                        sysNamesConstants.getFileMetadata(),
                        RequestFileMetadataContext.class,
                        ids.size(),
                        (current, step, total) -> new ElmaListRequest()
                                .ids(ids.subList(current * step, Math.min((current + 1) * step, total)))
                )
                .stream()
                .collect(Collectors.toMap(RequestFileMetadataContext::get__id, Function.identity()));
    }

    @NonNull
    public Map<String, RequestFileMetadataContext> getFileMetadataListByExternalIds(@NonNull List<String> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return elmaClient.list(
                        ecmProperties.getPathToDocuments(),
                        sysNamesConstants.getFileMetadata(),
                        RequestFileMetadataContext.class,
                        ids.size(),
                        (current, step, total) -> new ElmaListRequest()
                                .filter(InFilter.field("id_as", ids.subList(current * step, Math.min((current + 1) * step, total))))
                )
                .stream()
                .collect(Collectors.toMap(RequestFileMetadataContext::getId_as, Function.identity(), (a, b) -> a));
    }

    public List<Role> getActiveRoles() {
        return elmaClient.listAll(ecmProperties.getPathToReferences(), sysNamesConstants.getRoles(), Role.class, (current, step, total) ->
                new ElmaListRequest()
                        .filter(EqFilter.field("status", "active")));
    }

    public Optional<CreatorEditor> findCreatorEditor(String email, String idAs) {
        ElmaListResponse.Result<CreatorEditor> list = elmaClient.list(
                ecmProperties.getPathToReferences(),
                sysNamesConstants.getCreatorEditor(),
                CreatorEditor.class,
                new ElmaListRequest()
                        .filter(AndFilter.and(
                                EqFilter.field("email", email),
                                EqFilter.field("id_as", idAs)
                        ))
        );
        if (list.getResult().isEmpty() || list.getResult().get(0).getId().isBlank()) {
            return Optional.empty();
        } else {
            return Optional.of(list.getResult().get(0));
        }
    }

    public <T> T createReference(String path, T data) {
        return elmaClient.createReference(path, data);
    }

    public <T> T getDocument(String type, String id, Class<T> clazz) {
        return elmaClient.getDocument(type, id, clazz);
    }

    public void setFileMetadataStatus(String id, FileMetadataStatus status) {
        elmaClient.setDocumentStatus(sysNamesConstants.getFileMetadata(), id, status);
    }
}
