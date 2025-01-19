package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.properties.EcmApiConst;
import ru.bpmcons.sbi_elma.properties.EcmProperties;
import ru.bpmcons.sbi_elma.properties.SysNamesConstants;
import ru.bpmcons.sbi_elma.s3.S3FileUtils;
import ru.bpmcons.sbi_elma.s3.S3ModuleService;
import ru.bpmcons.sbi_elma.s3.dto.Bucket;
import ru.bpmcons.sbi_elma.service.PublicApiElmaService;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class GeneratePreviewMethod {
    private final EcmProperties ecmProperties;
    private final SysNamesConstants sysNamesConstants;
    private final PublicApiElmaService publicApiElmaService;
    private final S3ModuleService s3ModuleService;
    private final MessageWorkerService messageWorkerService;

    @Method("${methods.generatepreview}")
    public void doMethod(FileMetadata[] fileMetadata) {
        messageWorkerService.forEachParallel(
                Arrays.stream(fileMetadata)
                        .filter(fileMeta -> supportExtension(getExtension(fileMeta.getFile_name()))),
                this::generatePreview
        );
    }

    private void generatePreview(FileMetadata metadata) {
        String extension = getExtension(metadata.getFile_name());
        var preview = s3ModuleService.createPreview(metadata.isArchive() ? Bucket.ARCHIVE : Bucket.OPERATIVE, S3FileUtils.getFileId(metadata.getUrl_file()), extension);
        var previewUrl = metadata.getUrl_file().substring(0, metadata.getUrl_file().lastIndexOf('/')) + "/" + preview.getFileId();
        String bodyForRequestToEcm = createBodyForRequest(preview.getEcmId(), previewUrl, metadata);
        publicApiElmaService.doPost(bodyForRequestToEcm,
                ecmProperties.getPathToDocuments(),
                "/" + sysNamesConstants.getFileMetadata() + "/" + metadata.getId_ecm_filemetadata(),
                EcmApiConst.UPDATE
        );
    }

    private static boolean supportExtension(String extension) {
        return extension.startsWith("xl")
                || extension.startsWith("do")
                || extension.equals("pdf")
                || extension.startsWith("pp")
                || extension.startsWith("po")
                || extension.equals("jpeg")
                || extension.equals("jpg")
                || extension.equals("png")
                || extension.equals("bmp")
                || extension.equals("tiff")
                || extension.equals("gif")
                || extension.equals("csv")
                || extension.equals("rtf")
                || extension.equals("txt");
    }

    private static String getExtension(String fileMeta) {
        String[] split = fileMeta.split("\\.");
        return split[split.length - 1];
    }

    private String createBodyForRequest(String ecmId, String previewUrl, FileMetadata fileMetadata) {
        return "{\n" +
                "  \"context\": {\n" +
                "    \"preview\": [\n" +
                "\"" +  ecmId + "\"\n" +
                "    ],\n" +
                "    \"url_file\":" + "\"" + fileMetadata.getUrl_file() + "\"" + ",\n" +
                "    \"url_preview\":" + "\"" + previewUrl + "\"" + "\n" +
                "  }\n" +
                "}";
    }
}
