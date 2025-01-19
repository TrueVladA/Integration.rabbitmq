package ru.bpmcons.sbi_elma.service;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.base.EntityBase;
import ru.bpmcons.sbi_elma.elma.dto.common.ElmaListRequest;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.AndFilter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.EqFilter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.tf.TfFilter;
import ru.bpmcons.sbi_elma.elma.dto.common.filter.tf.operator.TimestampBetweenTfOperator;
import ru.bpmcons.sbi_elma.exceptions.CheckRequiredParametersException;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.models.dto.docLock.*;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.RequestSetStatus;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.Status;
import ru.bpmcons.sbi_elma.properties.*;
import ru.bpmcons.sbi_elma.s3.S3FileMetadata;
import ru.bpmcons.sbi_elma.security.SecurityContextHolder;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LockService {
    Logger logger = LoggerFactory.getLogger(LockService.class);

    static SimpleDateFormat FORMAT = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss");

    private final PublicApiElmaService publicApiElmaService;
    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;
    private final ObjectMapperService objectMapperService;
    private final MessageWorkerService messageWorkerService;

    public void checkLock(String id_ecm) {
        String body = createBodyListBlock(id_ecm);
        ResponseEntity<String> response = publicApiElmaService.doPost(body,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock(),
                EcmApiConst.LIST
        );
        ResponseFromEcmListBlock responseObject = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmListBlock.class);
        logger.debug(responseObject.toString());
        if (responseObject.getResult() != null && responseObject.getResult().getResult().length > 0) {
            Date block_to = responseObject.getResult().getResult()[0].getBlock_to();
            FORMAT.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
            throw new CheckRequiredParametersException(ResponseCodes.BLOCKED_INT, "Документ заблокирован. Дата окончания блокировки " + FORMAT.format(block_to));
        }
    }

    public void checkLockOnFiles(String id_ecm, List<String> files) {
        String body = createBodyListBlock(id_ecm);
        ResponseEntity<String> response = publicApiElmaService.doPost(body,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock(),
                EcmApiConst.LIST
        );
        ResponseFromEcmListBlock responseObject = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmListBlock.class);
        logger.debug(responseObject.toString());
        if (responseObject.getResult() != null && responseObject.getResult().getResult().length > 0) {
            Set<String> blockedFiles = new HashSet<>();
            for (BlockObject blockObject : responseObject.getResult().getResult()) {
                for (String file : files) {
                    if (Arrays.asList(blockObject.getFile_metadata_collection()).contains(file)) {
                        blockedFiles.add(file);
                    }
                }
            }

            if (!blockedFiles.isEmpty()) {
                Date block_to = responseObject.getResult().getResult()[0].getBlock_to();
                FORMAT.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
                String fileStr = String.join(",", blockedFiles);
                throw new CheckRequiredParametersException(ResponseCodes.BLOCKED_INT, "Файлы документа заблокированы: " + fileStr + ". Дата окончания блокировки " + FORMAT.format(block_to));
            }
        }
    }

    @Synchronized
    public synchronized boolean createLock(EntityBase entityBase, String[] updatedMetadata) {
        String body = createBodyLock(entityBase.getId(),
                entityBase.getSource(),
                entityBase.getCreator(),
                updatedMetadata);
        ResponseEntity<String> response = publicApiElmaService.doPost(body,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock(),
                EcmApiConst.CREATE
        );
        return response.getStatusCode().is2xxSuccessful();
    }

    @Synchronized
    public synchronized boolean unlock(String id_doc, String id_ecm) {
        String body = deleteBodyLock(id_ecm);
        ResponseEntity<String> response = publicApiElmaService.doPost(body,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock() + "/" + id_doc,
                EcmApiConst.UPDATE
        );
        return response.getStatusCode().is2xxSuccessful();
    }

    @Synchronized
    public synchronized boolean restoreLock(String docId, List<S3FileMetadata> metadata) {
        String bodyListBlockForRestore = createBodyListBlockForRestore(docId);
        ResponseEntity<String> responseFindBlockObject = publicApiElmaService.doPost(bodyListBlockForRestore,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock(),
                EcmApiConst.LIST
        );
        ResponseFromEcmListBlock responseObject = objectMapperService.getObjectFromJsonRequired(responseFindBlockObject.getBody(),
                ResponseFromEcmListBlock.class);
        BlockObject[] result = responseObject.getResult().getResult();
        if (result.length > 0) {
            var fileIds = metadata.stream().map(S3FileMetadata::getFileId).collect(Collectors.toSet());

            for (BlockObject blockObject : result) {
                if (blockObject.getFile_metadata_collection() != null) {
                    fileIds.addAll(Arrays.asList(blockObject.getFile_metadata_collection()));
                }
            }

            String body = createBodyLock(docId,
                    (result[0].getSource() != null && result[0].getSource().length > 0) ? result[0].getSource()[0] : null,
                    (result[0].getUser() != null && result[0].getUser().length > 0) ? result[0].getUser()[0] : null,
                    fileIds.toArray(String[]::new));
            RequestSetStatus requestSetStatus = new RequestSetStatus();
            Status status = new Status();
            status.setCode(UploadStatuses.getCode("Загружается"));
            requestSetStatus.setStatus(status);
            String updateBody = objectMapperService.getJsonFromObjectRequired(requestSetStatus);
            for (S3FileMetadata fmeta : metadata) {
                publicApiElmaService.doPost(updateBody,
                        ecmProperties.getPathToDocuments(),
                        sysNamesConstants.getFileMetadata() + "/" + fmeta.getFileId(),
                        EcmApiConst.SETSTATUS
                );
            }
            ResponseEntity<String> response = publicApiElmaService.doPost(body,
                    ecmProperties.getPathToDocuments(),
                    sysNamesConstants.getBlock() + "/" + result[0].get__id(),
                    EcmApiConst.UPDATE
            );
            return response.getStatusCode().is2xxSuccessful();
        } else {
            throw new IllegalStateException("Only works for blocks that are already created");
        }
    }

    @Synchronized
    public synchronized boolean restoreLock(EntityBase entityBase, List<String> updatedMetadata) {
        String bodyListBlockForRestore = createBodyListBlockForRestore(entityBase.getId());
        ResponseEntity<String> responseFindBlockObject = publicApiElmaService.doPost(bodyListBlockForRestore,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock(),
                EcmApiConst.LIST
        );
        ResponseFromEcmListBlock responseObject = objectMapperService.getObjectFromJsonRequired(responseFindBlockObject.getBody(),
                ResponseFromEcmListBlock.class);
        BlockObject[] result = responseObject.getResult().getResult();
        if (result.length > 0) {
            String body = createBodyLock(entityBase.getId(),
                    entityBase.getSource(),
                    entityBase.getEditor(),
                    updatedMetadata.toArray(String[]::new));
            messageWorkerService.forEachParallel(updatedMetadata, idMeta -> {
                RequestSetStatus requestSetStatus = new RequestSetStatus();
                Status status = new Status();
                status.setCode(UploadStatuses.getCode("Загружается"));
                requestSetStatus.setStatus(status);
                String updateBody = objectMapperService.getJsonFromObjectRequired(requestSetStatus);
                logger.debug(updateBody);
                publicApiElmaService.doPost(updateBody,
                        ecmProperties.getPathToDocuments(),
                        sysNamesConstants.getFileMetadata() + "/" + idMeta,
                        EcmApiConst.SETSTATUS
                );
            });
            ResponseEntity<String> response = publicApiElmaService.doPost(body,
                    ecmProperties.getPathToDocuments(),
                    sysNamesConstants.getBlock() + "/" + result[0].get__id(),
                    EcmApiConst.UPDATE
            );
            return response.getStatusCode().is2xxSuccessful();
        } else {
            return createLock(entityBase, updatedMetadata.toArray(String[]::new));
        }
    }

    @Synchronized
    public synchronized boolean updateOnNotificationLock(BlockObject blockObject) {
        String body = createOnNotificationBodyLock(blockObject.getId_ecm(),
                blockObject.getSource(),
                blockObject.getUser(),
                blockObject.getFile_metadata_collection()
        );
        ResponseEntity<String> response = publicApiElmaService.doPost(body,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock() + "/" + blockObject.get__id(),
                EcmApiConst.UPDATE
        );
        return response.getStatusCode().is2xxSuccessful();
    }

    private String createOnNotificationBodyLock(String id_ecm,
                                                String[] sourceId,
                                                String[] creator,
                                                String[] file_metadata) {
        RequestCreateBlock request = new RequestCreateBlock();
        CreateContext context = CreateContext.builder()
                .id_ecm(id_ecm)
                .source(sourceId)
                .creator_editor(creator)
                .user_data(SecurityContextHolder.getPrincipalName())
                .file_metadata_collection(file_metadata)
                .build();
        request.setContext(context);
        String body = objectMapperService.getJsonFromObjectRequired(request);
        logger.debug(body);
        return body;
    }

    private String createBodyLock(String id_ecm,
                                  String sourceId,
                                  String creator,
                                  String[] file_metadata) {
        RequestCreateBlock request = new RequestCreateBlock();
        Date blockFrom = new Date();
        Date blockTo = new Date(blockFrom.getTime() + 900000);
        CreateContext context = CreateContext.builder()
                .id_ecm(id_ecm)
                .source(sourceId == null ? new String[]{} : new String[]{sourceId})
                .user(creator == null ? new String[]{} : new String[]{creator})
                .creator_editor(creator == null ? new String[]{} : new String[]{creator})
                .block_from(blockFrom)
                .block_to(blockTo)
                .user_data(SecurityContextHolder.getPrincipalName())
                .file_metadata_collection(file_metadata)
                .build();
        request.setContext(context);
        String body = objectMapperService.getJsonFromObjectRequired(request);
        logger.debug(body);
        return body;
    }

    private String deleteBodyLock(String id_ecm) {
        RequestDeleteBlock request = new RequestDeleteBlock();
        DeleteContext context = DeleteContext.builder()
                .id_ecm(id_ecm)
                .block_to(null)
                .block_from(null)
                .user_data(null)
                .file_metadata_collection(new String[]{})
                .build();
        request.setContext(context);
        String body = objectMapperService.getJsonFromObjectRequired(request);
        logger.debug(body);
        return body;
    }

    public ResponseFromEcmListBlock findBlock(String id_ecm) {
        String body = createBodyListBlockForRestore(id_ecm);
        ResponseEntity<String> response = publicApiElmaService.doPost(body,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock(),
                EcmApiConst.LIST
        );
        return objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmListBlock.class);
    }

    private String createBodySearchExpiredLock() {
        String body = objectMapperService.getJsonFromObjectRequired(
                new ElmaListRequest()
                        .size(100)
                        .filter(TfFilter.field(
                                "block_to",
                                TimestampBetweenTfOperator.to(
                                        ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(1L)
                                )
                        ))
        );
        logger.debug(body);
        return body;
    }

    private String createBodyListBlock(String id_ecm) {
        String body = objectMapperService.getJsonFromObjectRequired(
                new ElmaListRequest()
                        .size(100)
                        .filter(AndFilter.and(
                                EqFilter.field("id_ecm", id_ecm),
                                TfFilter.field("block_to",
                                        TimestampBetweenTfOperator.from(ZonedDateTime.now(ZoneId.of("UTC")))
                                )
                        )));
        logger.debug(body);
        return body;
    }

    private String createBodyListBlockForRestore(String id_ecm) {
        String body = objectMapperService.getJsonFromObjectRequired(new ElmaListRequest()
                        .size(100)
                        .filter(EqFilter.field("id_ecm", id_ecm)));
        logger.debug(body);
        return body;
    }

    @Scheduled(fixedDelayString = "120000")
    public void docTypeRepeatDownload() {
//        logger.info("---------------Запуск процесса удаления просроченных объектов блокировки---------------");
        String body = createBodySearchExpiredLock();
        ResponseEntity<String> response = publicApiElmaService.doPost(body,
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getBlock(),
                EcmApiConst.LIST
        );
        ResponseFromEcmListBlock responseObject = objectMapperService.getObjectFromJsonRequired(response.getBody(),
                ResponseFromEcmListBlock.class);
//        logger.info(String.valueOf(responseObject.getResult().getResult().length));
        Arrays.stream(responseObject.getResult().getResult()).forEach(blockObject -> {
            Arrays.stream(blockObject.getFile_metadata_collection()).forEach(s -> {
                RequestSetStatus requestSetStatus = new RequestSetStatus();
                Status status = new Status();
                status.setCode(UploadStatuses.getCode("Не загружен"));
                requestSetStatus.setStatus(status);
                String updateBody = objectMapperService.getJsonFromObjectRequired(requestSetStatus);
//                logger.info(updateBody);
                publicApiElmaService.doPost(updateBody,
                        ecmProperties.getPathToDocuments(),
                        sysNamesConstants.getFileMetadata() + "/" + s,
                        EcmApiConst.SETSTATUS
                );
            });
            unlock(blockObject.get__id(), blockObject.getId_ecm());
        });
//        logger.info("---------------Процесс удаления просроченных объектов блокировки окончен---------------");
    }
}
