package ru.bpmcons.sbi_elma.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.bpmcons.sbi_elma.ecm.dto.dict.FileType;
import ru.bpmcons.sbi_elma.ecm.dto.reference.OperationName;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.FileTypeRepository;
import ru.bpmcons.sbi_elma.properties.S3ModuleProperties;
import ru.bpmcons.sbi_elma.s3.exception.PresignUrlInvalidException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileUtils {
    private final S3ModuleProperties properties;
    private final FileTypeRepository fileTypeRepository;
    private final DocTypeRepository docTypeRepository;
    private final ContractTypeRepository contractTypeRepository;

    public static String getFileId(String urlFile) {
        if (urlFile.indexOf('/') == -1) {
            return urlFile;
        }
        String[] list = urlFile.split("/");
        return list[list.length - 1];
    }

    public S3FileMetadata parse(String url) {
        try {

            Map<String, String> map = UriComponentsBuilder.fromUriString(url)
                    .build()
                    .getQueryParams()
                    .toSingleValueMap()
                    .entrySet()
                    .stream()
                    .filter(s -> s.getKey().startsWith("X-ECM-Attr-"))
                    .collect(Collectors.toMap(s -> s.getKey().replace("X-ECM-Attr-", ""), Map.Entry::getValue));
            OperationName operationName = OperationName.valueOf(map.get("Op"));
            FileType fileType = fileTypeRepository.findById(map.get("FileType"));
            if (map.containsKey("DUL")) {
                return new S3IdentityDocFileMetadata(fileType, operationName, map.get("DocID"), map.get("FileID"));
            } else {
                return new S3FileMetadata(
                        fileType,
                        map.containsKey("DocType") ? docTypeRepository.findById(map.get("DocType")) : null,
                        map.containsKey("ContractType") ? contractTypeRepository.findById(map.get("ContractType")) : null,
                        operationName,
                        map.get("DocID"),
                        map.get("FileID")
                );
            }
        } catch (NullPointerException e) {
            log.error("Failed to parse presign", e);
            throw new PresignUrlInvalidException();
        }
    }

    public String migrateUrlToArchive(String urlFile) {
        return urlFile.replace(properties.getOperativeBucket(), properties.getArchiveBucket());
    }
}
