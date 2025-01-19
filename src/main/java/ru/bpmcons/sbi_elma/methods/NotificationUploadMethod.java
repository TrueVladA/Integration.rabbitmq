package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.ecm.EcmService;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataStatus;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.models.dto.docLock.BlockObject;
import ru.bpmcons.sbi_elma.models.dto.docLock.ResponseFromEcmListBlock;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.RequestFileMetadataContext;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.ResponseFromEcmGetFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.request.NotificationUploadRequest;
import ru.bpmcons.sbi_elma.properties.EcmApiConst;
import ru.bpmcons.sbi_elma.properties.EcmProperties;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.service.LockService;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class NotificationUploadMethod {
    public static final String SLASH = "/";
    Logger logger = LoggerFactory.getLogger(NotificationUploadMethod.class);

    private final ObjectMapperService objectMapperService;
    private final PublicApiElmaService publicApiElmaService;
    private final LockService lockService;
    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;
    private final GeneratePreviewMethod generatePreviewMethod;
    private final CreateOrUpdateFileMetadata createOrUpdateFileMetadata;
    private final EcmService ecmService;
    private final MessageWorkerService messageWorkerService;

    @Validated
    @Method("${methods.notificationUpload}")
    public void doMethod(@Valid NotificationUploadRequest notificationObject) {
        try {
            synchronized (this) {
                ResponseFromEcmListBlock block = lockService.findBlock(notificationObject.getEcmId());
                logger.debug(block.toString());
                BlockObject[] result = block.getResult().getResult();
                if (result.length == 0) {
                    logger.error("Блокировки с id_ecm = " + notificationObject.getEcmId() + " не найдено");
                    return;
                }
                Set<String> listIdMetadata = Arrays.stream(result[0].getFile_metadata_collection()).collect(Collectors.toSet());
                List<String> listIdMetadataForGenPreview = messageWorkerService.runInWorker(() -> notificationObject.getFiles()
                        .stream()
                        .filter(fileNotification -> listIdMetadata.remove(fileNotification.getEcmId()))
                        .parallel()
                        .map(notificationFile -> {
                            FileMetadataStatus ecmStatus = new FileMetadataStatus(notificationFile.getUploadStatus());
                            ecmService.setFileMetadataStatus(notificationFile.getEcmId(), ecmStatus);
                            return notificationFile.getEcmId();
                        })
                        .collect(Collectors.toList()));
                if (listIdMetadata.isEmpty()) {
                    lockService.unlock(result[0].get__id(), result[0].getId_ecm());
                } else {
                    result[0].setFile_metadata_collection(listIdMetadata.toArray(String[]::new));
                    lockService.updateOnNotificationLock(result[0]);
                }
            }
            /**
             * Отключена генерация preview 19-10-2023. Раскомментировать для включения
             */
//        generatePreview(messageProperties, listIdMetadataForGenPreview);
        } catch (Exception e) {
            logger.error("Ошибка NotificationUploadMethod", e);
        }
    }

    private void generatePreview(List<String> listIdMetadataForGenPreview) {
        listIdMetadataForGenPreview.forEach(id -> {
            ResponseEntity<String> responseEntity = publicApiElmaService.doPost("",
                    ecmProperties.getPathToDocuments(),
                    sysNamesConstants.getFileMetadata() + "/" + id,
                    EcmApiConst.GET
            );
            ResponseFromEcmGetFileMetadata responseObject = objectMapperService.getObjectFromJsonRequired(responseEntity.getBody(),
                    ResponseFromEcmGetFileMetadata.class);
            if (responseObject.getItem() != null) {
                RequestFileMetadataContext item = responseObject.getItem();
                FileMetadata fileMetadata = createOrUpdateFileMetadata.getFileMetadataArray(item);
                generatePreviewMethod.doMethod(new FileMetadata[]{fileMetadata});
            }
        });
    }
}
