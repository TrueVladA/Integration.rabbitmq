package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.bpmcons.sbi_elma.Versions;
import ru.bpmcons.sbi_elma.ecm.EcmService;
import ru.bpmcons.sbi_elma.ecm.dto.dict.CommonSystem;
import ru.bpmcons.sbi_elma.ecm.dto.dict.FileProject;
import ru.bpmcons.sbi_elma.ecm.dto.dict.FileType;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataRow;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.dto.trait.FileMetadataTrait;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.FileProjectRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.FileStatusRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.FileTypeRepository;
import ru.bpmcons.sbi_elma.exceptions.S3UnavailableException;
import ru.bpmcons.sbi_elma.infra.version.Version;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.methods.additional.CommonMethods;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.*;
import ru.bpmcons.sbi_elma.models.dto.generralized.*;
import ru.bpmcons.sbi_elma.models.dto.preSignUrl.ResponseCreateEmptyFile;
import ru.bpmcons.sbi_elma.properties.*;
import ru.bpmcons.sbi_elma.s3.S3FileUtils;
import ru.bpmcons.sbi_elma.s3.S3Metadata;
import ru.bpmcons.sbi_elma.s3.S3ModuleService;
import ru.bpmcons.sbi_elma.s3.dto.Bucket;
import ru.bpmcons.sbi_elma.s3.dto.Method;
import ru.bpmcons.sbi_elma.s3.dto.PresignResponse;
import ru.bpmcons.sbi_elma.security.file.authorization.FileAuthorizationService;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;
import ru.bpmcons.sbi_elma.utils.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Сервис для работы с метаданными
 */
@Component
@RequiredArgsConstructor
public class CreateOrUpdateFileMetadata {
    public static final String SLASH = "/";
    Logger logger = LoggerFactory.getLogger(CreateOrUpdateFileMetadata.class);

    private final ObjectMapperService objectMapperService;
    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;
    private final PublicApiElmaService publicApiElmaService;
    private final S3ModuleProperties s3ModuleProperties;
    private final S3ModuleService s3ModuleService;
    private final FileAuthorizationService fileAuthorizationService;
    private final EcmService ecmService;
    private final FileTypeRepository fileTypeRepository;
    private final FileProjectRepository fileProjectRepository;
    private final FileStatusRepository fileStatusRepository;
    private final MessageWorkerService messageWorkerService;


    public FileMetadata[] createOrUpdateFileMetadata(@Nullable FileMetadataTable existDoc,
                                                     GeneralizedDoc generalizedDoc,
                                                     CommonSystem commonSystem,
                                                     Version currentVersion) {
        ConcurrentHashMap<String, FileMetadata> resultFileMetadata = new ConcurrentHashMap<String, FileMetadata>();
        addExistMetadataInResult(existDoc, resultFileMetadata);
        FileMetadata[] file_metadata = generalizedDoc.getFile_metadata();
        addNewMetadataInResult(file_metadata, generalizedDoc, s3ModuleProperties.getPutMethod(), resultFileMetadata, commonSystem, currentVersion);
        if (resultFileMetadata.isEmpty()) {
            return new FileMetadata[]{};
        } else {
            int size = resultFileMetadata.values().size();
            return resultFileMetadata.values().toArray(new FileMetadata[size]);
        }
    }

    public FileMetadata[] createOrUpdateFileMetadataIdentityDoc(FileMetadataTrait existDoc,
                                                                IdentityDoc identityDoc,
                                                                String preSignMethod,
                                                                CommonSystem commonSystem,
                                                                Version currentVersion) {
        ConcurrentHashMap<String, FileMetadata> resultFileMetadata = new ConcurrentHashMap<String, FileMetadata>();
        addExistMetadataInResult(existDoc == null ? null : existDoc.getFileMetadata(), resultFileMetadata);
        FileMetadata[] file_metadata = identityDoc.getFile_metadata();
        addNewMetadataInResult(file_metadata, null, preSignMethod, resultFileMetadata, commonSystem, currentVersion);
        if (resultFileMetadata.isEmpty()) {
            return new FileMetadata[]{};
        } else {
            int size = resultFileMetadata.values().size();
            return resultFileMetadata.values().toArray(new FileMetadata[size]);
        }
    }

    /**
     * Метод проверяет существует ли карточка метаданных в elma. Если нет - создаёт, если нет - обновляет context метаданных
     * @param file_metadata метаданные запроса АС
     * @param generalizedDoc обобщёная схема
     * @param preSignMethod метод для формирования ссылки preSign
     * @param resultFileMetadata справочник метаданных
     */
    private void addNewMetadataInResult(FileMetadata[] file_metadata,
                                        @Nullable GeneralizedDoc generalizedDoc,
                                        String preSignMethod,
                                        ConcurrentHashMap<String, FileMetadata> resultFileMetadata,
                                        CommonSystem commonSystem,
                                        Version currentVersion) {

        if (file_metadata != null && file_metadata.length > 0) {
            FileAuthorizationService.Context fileAuthCtx = generalizedDoc == null ? fileAuthorizationService.buildNoopContext() : fileAuthorizationService.buildContext(generalizedDoc);
            boolean useEcmId = currentVersion.isNotBefore(Versions.V_1_1_18);
            Map<String, RequestFileMetadataContext> found = useEcmId
                ? ecmService.getFileMetadataList(
                        Arrays.stream(file_metadata)
                                .map(FileMetadata::getId_ecm_filemetadata)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                )
                : ecmService.getFileMetadataListByExternalIds(
                        Arrays.stream(file_metadata)
                                .map(FileMetadata::getId_as_filemetadata)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );

            messageWorkerService.forEachParallel(file_metadata, metadata -> {
                RequestFileMetadataContext fileMetadata = found.get(useEcmId ? metadata.getId_ecm_filemetadata() : metadata.getId_as_filemetadata());
                ResponseFromEcmCreateFileMetadata responseFileMetadata;
                if (fileMetadata == null) {
                    fileAuthCtx.requirePermission(metadata, OperationName.CREATE);
                    responseFileMetadata = createFileMetadata(metadata, preSignMethod, commonSystem.getId());
                } else {
                    metadata.setId_ecm_filemetadata(fileMetadata.get__id());
                    fileAuthCtx.requirePermission(metadata, OperationName.UPDATE);
                    responseFileMetadata = updateFileMetadata(metadata, fileMetadata, currentVersion);
                }
                String id = responseFileMetadata.getItem().get__id();
                metadata.setId_ecm_filemetadata(id);
                resultFileMetadata.put(id, getFileMetadataArray(responseFileMetadata.getItem()));
            });
        }
    }

    /**
     * Метод предназначен для формирования объектов класса FileMetadata для уже существующих метаданных
     * Так как api elma на запрос /get какого-либо документа отдаёт нам таблицу метаданных в виде [id_meta1, id_meta2, ...],
     * без типа файла, ссылок, расширений и тд, то приходится отдельно запрашивать /get для метаданных, чтобы получить
     * это информацию, без неё невозможно проверить доступ к файлам
     * @param existDoc может быть null, если метод вызывается в рамках createDoc
     * @param resultFileMetadata справочник метаданных в рамках запроса
     */
    private void addExistMetadataInResult(@Nullable FileMetadataTable existDoc, ConcurrentHashMap<String, FileMetadata> resultFileMetadata) {
        if (existDoc != null
                && existDoc.getRows() != null
                && !existDoc.getRows().isEmpty()) {
            ecmService.getFileMetadataList(
                        existDoc.getRows().stream()
                            .map(FileMetadataRow::getFileMetadata)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
                    )
                    .forEach((s, ctx) -> resultFileMetadata.put(s, getFileMetadataArray(ctx)));
        }
    }

    public FileMetadataTable createTableFileMetadata(FileMetadata[] fileMetadata) {
        FileMetadataTable fileMetadataTable = new FileMetadataTable();
        fileMetadataTable.setRows(
                Arrays.stream(fileMetadata)
                        .map(meta -> {
                            FileMetadataRow row = new FileMetadataRow();
                            row.setFileMetadata(meta.getId_ecm_filemetadata());
                            row.setFileName(meta.getFile_name());
                            row.setCategories(meta.getCategories());
                            fileTypeRepository.findByFileTypeIdOptional(meta.getFile_type().getSingleValue())
                                    .ifPresent(fileType -> {
                                        row.setFileType(fileType.getId());
                                        row.setOldFileType(fileType.getId());
                                    });

                            return row;
                        })
                        .collect(Collectors.toList())
        );
        return fileMetadataTable;
    }

    /**
     * Конвертор, переводящий context ответа от public api elma в сущность класса FileMetadata
     * @param item context метаданных в elma
     * @return
     */
    public FileMetadata getFileMetadataArray(RequestFileMetadataContext item) {
        FileMetadata file_metadata = new FileMetadata();
        file_metadata.setId_as_filemetadata(item.getId_as());
        file_metadata.setId_ecm_filemetadata(item.get__id());
        file_metadata.setFile_name(item.getFile_name());
        file_metadata.setCategories(item.getCategories());
        file_metadata.setCreate_date(item.getCreate_date());
        file_metadata.setUpdate_date(item.getChange_date());
        file_metadata.setVersion_number(item.getVersion_number());
        file_metadata.setCurrent_version(item.isCurrent_version());
        file_metadata.setMedical_doc(item.isMedical_doc());
        file_metadata.setUrl_as(item.getUrl_as());
        file_metadata.setArchive(item.isArchive());
        Optional<FileType> fileTypeObject = fileTypeRepository.findByIdOptional(item.getFile_type()[0]);

        fileTypeObject.ifPresent(fileType -> {
//            Optional<CommonSystem> commonSystem = cacheService.searchCommSystemFromEcm(Optional.ofNullable(messageProperties.getAppId()));
            PerecoderObject perecoderObject = CommonMethods.createPerecoderObject(PerecoderDictNames.FILETYPES.getName(),
                    PerecoderGroupNames.EADOC.getName(),
                    fileType.getFileTypeId());
            file_metadata.setFile_type(perecoderObject);
        });
        file_metadata.setDoc_size(item.getFile_size());
        file_metadata.setCrc(item.getCrc());
        file_metadata.setFile_name(item.getFile_name());
        file_metadata.setEsign(new Signature());
        file_metadata.setUrl_file(item.getUrl_file());
        file_metadata.setToken_file(item.getUrl_file());
        file_metadata.setUrl_preview(item.getUrl_preview());
        file_metadata.setVersion_number(item.getVersion_number());
        ru.bpmcons.sbi_elma.models.dto.doc.Status status = item.get__status();
        if (status != null) {
            file_metadata.setUpload_status(UploadStatuses.getStatus(status.getStatus()));
        } else {
            file_metadata.setUpload_status(UploadStatuses.getStatus("uploading"));
        }
        if (item.getProject() != null) {
            FileProject project = fileProjectRepository.findById(item.getProject());
            file_metadata.setProject(project.getSysName());
        }
        return file_metadata;
    }


    public String getPresignUrl(String file, boolean archive, boolean disableBucketCheck, S3Metadata metadata) {
        if (file == null || file.isBlank()) {
            return "";
        }
        PresignResponse res = s3ModuleService.presign(S3FileUtils.getFileId(file), Method.GET, disableBucketCheck ? null : (archive ? Bucket.ARCHIVE : Bucket.OPERATIVE), null, metadata);
        if (res == null) {
            return "";
        }
        return res.getPresignUrl();
    }


    public String putPresignUrl(String file, boolean archive, boolean disableBucketCheck, String md5, S3Metadata metadata) {
        if (file == null || file.isBlank()) {
            return "";
        }
        PresignResponse res = s3ModuleService.presign(S3FileUtils.getFileId(file), Method.PUT, disableBucketCheck ? null : (archive ? Bucket.ARCHIVE : Bucket.OPERATIVE), md5, metadata);
        if (res == null) {
            return "";
        }
        return res.getPresignUrl();
    }

    @NonNull
    public ResponseFromEcmCreateFileMetadata createFileMetadata(FileMetadata fileMetadata,
                                                                 String preSignMethod,
                                                                 String appId) {
        String requestBody = createFileMetadataBodyForEcmRequest(fileMetadata, preSignMethod, appId);
        logger.debug(requestBody);
        ResponseEntity<String> response = publicApiElmaService.doPost(requestBody,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getFileMetadata(),
                EcmApiConst.CREATE
        );
        logger.debug(response.getBody());
        String responseBody = response.getBody();
        return objectMapperService.getObjectFromJsonRequired(responseBody,
                ResponseFromEcmCreateFileMetadata.class);
    }

    @NonNull
    public ResponseFromEcmCreateFileMetadata updateFileMetadata(FileMetadata fileMetadata,
                                                                 RequestFileMetadataContext ecmFilemetadata,
                                                                Version version) {
        String requestBody = updateFileMetadataBodyForEcmRequest(fileMetadata, ecmFilemetadata, version);
        ResponseEntity<String> response = publicApiElmaService.doPost(requestBody,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getFileMetadata() + SLASH + ecmFilemetadata.get__id(),
                EcmApiConst.UPDATE
        );
        String responseBody = response.getBody();
        return objectMapperService.getObjectFromJsonRequired(responseBody,
                ResponseFromEcmCreateFileMetadata.class);
    }

    private String createFileMetadataBodyForEcmRequest(FileMetadata fileMetadata, String preSignMethod, String appId) {
        RequestCreateFileMetadata body = new RequestCreateFileMetadata();
        RequestFileMetadataContext context = new RequestFileMetadataContext();
        body.setContext(context);
        context.setArchive(false);
        context.setCreate_date(fileMetadata.getCreate_date());
        context.setChange_date(fileMetadata.getUpdate_date());
        context.setCrc(fileMetadata.getCrc());
        if (fileMetadata.getCategories() != null) {
            String categories = Utils.createMarkdown(fileMetadata.getCategories());
            context.setCategories(categories);
        }
        context.setFile_name(fileMetadata.getFile_name());
        context.setMedical_doc(fileMetadata.isMedical_doc());
        context.setCurrent_version(fileMetadata.isCurrent_version());
        context.setId_as(fileMetadata.getId_as_filemetadata());
        context.setVersion_number(fileMetadata.getVersion_number());
        context.setUrl_as(fileMetadata.getUrl_as());
        context.setFile_size(fileMetadata.getDoc_size());
        if (fileMetadata.getFile_type() != null) {
            Optional<FileType> fileTypeBySysName = fileTypeRepository.findByFileTypeIdOptional(fileMetadata.getFile_type().getDictValues()[0].getDictValue().getValue());
            fileTypeBySysName.ifPresent(fileType -> context.setFile_type(new String[]{fileType.getId()}));
        }
        context.setSource(new String[]{appId});
        if (fileMetadata.getProject() != null) {
            FileProject project = fileProjectRepository.findBySysName(fileMetadata.getProject());
            context.setProject(project.getId());
        }
        String fileUuid = UUID.randomUUID().toString();
        if (preSignMethod.equals("put")) {
            String fileName = fileMetadata.getFile_name();
            ResponseEntity<String> response = publicApiElmaService.uploadEmptyFile(fileName, fileUuid);
            if (response.getStatusCode() == HttpStatus.valueOf(200)) {
                ResponseCreateEmptyFile responseWithFileData = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                        ResponseCreateEmptyFile.class);
                context.setFile(new String[]{responseWithFileData.getFile().get__id()});
                context.setUrl_file("https://" + s3ModuleProperties.getAddress() + SLASH + s3ModuleProperties.getOperativeBucket() + SLASH + fileUuid);
            } else {
                throw new S3UnavailableException(s3ModuleProperties.getOperativeBucket() + " bucket в хранилище файлов не доступен");
            }
        }
        FileMetadataVersion ver = context.toVersion(1, fileStatusRepository);
        context.setVersions(new FileMetadataVersionTable(List.of(ver)));
        logger.debug(context.toString());
        return objectMapperService.getJsonFromObjectRequired(body);
    }

    private String updateFileMetadataBodyForEcmRequest(FileMetadata fileMetadata,
                                                       RequestFileMetadataContext existing, Version currentVer) {
        RequestCreateFileMetadata body = new RequestCreateFileMetadata();
        RequestFileMetadataContext context = new RequestFileMetadataContext();
        body.setContext(context);

        boolean metaSame = Objects.equals(fileMetadata.getCrc(), existing.getCrc())
                && Objects.equals(Utils.createMarkdown(fileMetadata.getCategories()), existing.getCategories())
                && Objects.equals(fileMetadata.getFile_name(), existing.getFile_name())
                && Objects.equals(fileMetadata.getId_as_filemetadata(), existing.getId_as())
                && Objects.equals(fileMetadata.getVersion_number(), existing.getVersion_number())
                && Objects.equals(fileMetadata.getUrl_as(), existing.getUrl_as())
                && Objects.equals(fileMetadata.getDoc_size(), existing.getFile_size())
                && Objects.equals(fileMetadata.getProject() == null ? null : fileProjectRepository.findBySysName(fileMetadata.getProject()).getId(), existing.getProject());

        context.setArchive(false);
        context.setCreate_date(fileMetadata.getCreate_date());
        if (fileMetadata.getUpdate_date() == null) {
            context.setChange_date(new Date());
        } else {
            context.setChange_date(fileMetadata.getUpdate_date());
        }
        context.setCrc(fileMetadata.getCrc());
        if (fileMetadata.getCategories() != null) {
            String categories = Utils.createMarkdown(fileMetadata.getCategories());
            context.setCategories(categories);
        }
        context.setFile_name(fileMetadata.getFile_name());
        context.setMedical_doc(fileMetadata.isMedical_doc());
        context.setCurrent_version(fileMetadata.isCurrent_version());
        context.setId_as(fileMetadata.getId_as_filemetadata());
        context.setVersion_number(fileMetadata.getVersion_number());
        context.setUrl_as(fileMetadata.getUrl_as());
        context.setFile_size(fileMetadata.getDoc_size());
        context.setSource(existing.getSource());
        if (fileMetadata.getFile_type() != null) {
            Optional<FileType> fileTypeBySysName = fileTypeRepository.findByFileTypeIdOptional(fileMetadata.getFile_type().getDictValues()[0].getDictValue().getValue());
            fileTypeBySysName.ifPresent(fileType -> context.setFile_type(new String[]{fileType.getId()}));
        }
        if (fileMetadata.getProject() != null) {
            FileProject project = fileProjectRepository.findBySysName(fileMetadata.getProject());
            context.setProject(project.getId());
        }
        String fileUuid = UUID.randomUUID().toString();
        if (!metaSame) {
            if (existing.getVersions() == null) {
                existing.setVersions(new FileMetadataVersionTable(new ArrayList<>()));
            }
            if (existing.getVersions().getRows() == null) {
                existing.getVersions().setRows(new ArrayList<>());
            }
            int ver = existing.getVersions().getRows().stream().mapToInt(FileMetadataVersion::getVer).max().orElse(1);

            if (!Objects.equals(fileMetadata.getDoc_size(), existing.getFile_size()) || !Objects.equals(fileMetadata.getCrc(), existing.getCrc())) {
                String fileName = fileMetadata.getFile_name();
                ResponseEntity<String> response = publicApiElmaService.uploadEmptyFile(fileName, fileUuid);
                if (response.getStatusCode() == HttpStatus.valueOf(200)) {
                    ResponseCreateEmptyFile responseWithFileData = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                            ResponseCreateEmptyFile.class);
                    context.setFile(new String[]{responseWithFileData.getFile().get__id()});
                    context.setUrl_file("https://" + s3ModuleProperties.getAddress() + SLASH + s3ModuleProperties.getOperativeBucket() + SLASH + fileUuid);
                } else {
                    throw new S3UnavailableException(s3ModuleProperties.getOperativeBucket() + " bucket в хранилище файлов не доступен");
                }

            }

            if (currentVer.isNotBefore(Versions.V_1_1_20)) {
                FileMetadataVersion newVer = context.toVersion(ver + 1, fileStatusRepository);
                existing.getVersions().getRows().add(newVer);
            } else {
                existing.getVersions().getRows().clear();
                FileMetadataVersion newVer = context.toVersion(1, fileStatusRepository);
                existing.getVersions().getRows().add(newVer);
            }
        }
        context.setVersions(existing.getVersions());
        logger.debug(context.toString());
        return objectMapperService.getJsonFromObjectRequired(body);
    }

    /**
     * Конвертор, переводящий массив айдишников таблицы метаданных документа, в объекты FileMetadata
     * На основе массива формируется ответ json на getDoc
     * @param rows метаданные таблицы "метаданные" какого-либо документа elma
     * @return массив объектов dto метаданных
     */
    public List<FileMetadata> getFileMetadataArray(List<FileMetadataRow> rows) {
        List<FileMetadata> fileMetadataList = new CopyOnWriteArrayList<>();
//        HashMap<String, FileMetadata> fileMeta = new HashMap<>();
        if (rows != null) {
            messageWorkerService.forEachParallel(rows, fileMetadataRow -> {
                if (fileMetadataRow.getFileMetadata() != null) {
                    String fileMetaId = fileMetadataRow.getFileMetadata();
                    ResponseEntity<String> responseEntity = publicApiElmaService.doPost("",
                            ecmProperties.getPathToDocuments(),
                            sysNamesConstants.getFileMetadata() + SLASH + fileMetaId,
                            EcmApiConst.GET
                    );
                    ResponseFromEcmGetFileMetadata responseObject = objectMapperService.getObjectFromJsonRequired(responseEntity.getBody(),
                            ResponseFromEcmGetFileMetadata.class);
                    if (responseObject.getItem() != null) {
                        RequestFileMetadataContext item = responseObject.getItem();
                        FileMetadata fileMetadata = getFileMetadataArray(item);
//                    fileMeta.put(fileMetaId, fileMetadata);
                        fileMetadataList.add(fileMetadata);
                    }
                }
            });
        }
        return fileMetadataList;
    }
}
