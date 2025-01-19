package ru.bpmcons.sbi_elma.methods.additional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.models.dto.DeleteDocContext;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.FileMetadataVersion;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.RequestFileMetadataContext;
import ru.bpmcons.sbi_elma.properties.EcmApiConst;
import ru.bpmcons.sbi_elma.properties.EcmProperties;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.s3.S3FileUtils;
import ru.bpmcons.sbi_elma.s3.S3ModuleService;
import ru.bpmcons.sbi_elma.service.ObjectMapperService;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;

@Service
@RequiredArgsConstructor
public class DeleteFileService {
    private final S3ModuleService s3ModuleService;
    private final S3FileUtils fileUtils;
    private final PublicApiElmaService publicApiElmaService;
    private final ObjectMapperService objectMapperService;
    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;

    public void delete(RequestFileMetadataContext fileMetadata) {
        String fileMetadataId = fileMetadata.get__id();

        DeleteDocContext context = new DeleteDocContext();
        context.setArchive(true);

        if (fileMetadata.getVersions() == null || fileMetadata.getVersions().getRows() == null) {
            if (fileMetadata.getUrl_file() != null && !fileMetadata.getUrl_file().isBlank()) {
                s3ModuleService.archive(S3FileUtils.getFileId(fileMetadata.getUrl_file()));
            }
            if (fileMetadata.getUrl_preview() != null && !fileMetadata.getUrl_preview().isBlank()) {
                s3ModuleService.archive(S3FileUtils.getFileId(fileMetadata.getUrl_preview()));
            }
        } else {
            for (FileMetadataVersion version : fileMetadata.getVersions().getRows()) {
                version.setArchive(true);
                if (version.getUrl_file() != null && !version.getUrl_file().isBlank()) {
                    s3ModuleService.archive(S3FileUtils.getFileId(version.getUrl_file()));
                    version.setUrl_file(fileUtils.migrateUrlToArchive(version.getUrl_file()));
                }
                if (version.getUrl_preview() != null && !version.getUrl_preview().isBlank()) {
                    s3ModuleService.archive(S3FileUtils.getFileId(version.getUrl_preview()));
                    version.setUrl_preview(fileUtils.migrateUrlToArchive(version.getUrl_preview()));
                }
            }
        }

        if (fileMetadata.getUrl_file() != null && !fileMetadata.getUrl_file().isBlank()) {
            context.setUrl_file(fileUtils.migrateUrlToArchive(fileMetadata.getUrl_file()));
        }
        if (fileMetadata.getUrl_preview() != null && !fileMetadata.getUrl_preview().isBlank()) {
            context.setUrl_preview(fileUtils.migrateUrlToArchive(fileMetadata.getUrl_preview()));
        }
        ru.bpmcons.sbi_elma.models.dto.RequestDeleteDoc object = new ru.bpmcons.sbi_elma.models.dto.RequestDeleteDoc();
        object.setContext(context);
        publicApiElmaService.doPost(objectMapperService.getJsonFromObjectRequired(object),
                ecmProperties.getPathToDocuments(),
                sysNamesConstants.getFileMetadata() + "/" + fileMetadataId,
                EcmApiConst.UPDATE
        );
    }
}
