package ru.bpmcons.sbi_elma.security.file.extensions;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.bpmcons.sbi_elma.models.ability.FileMetadataContainer;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DeniedFileExtensionRepository;
import ru.bpmcons.sbi_elma.exceptions.CheckRequiredParametersException;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class CheckDeniedFileExtensionsAspect {
    private final DeniedFileExtensionRepository deniedFileExtensionRepository;

    @Before("@annotation(CheckDeniedFileExtensions) && args(arg, ..)")
    public void authorize(FileMetadataContainer arg) {
        Map<String, String> result = findDeniedExtensions(arg);

        if (!result.isEmpty()) {
            String msg = buildMessage(result);
            throw new CheckRequiredParametersException(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), msg);
        }
    }

    private static String buildMessage(Map<String, String> result) {
        StringBuilder builder = new StringBuilder();
        builder.append("В документе присутствуют файлы с запрещённым расширением: ");
        result.forEach((key, value) -> {
            builder.append(key);
            builder.append(" : '");
            builder.append(value);
            builder.append("', ");
        });
        builder.deleteCharAt(builder.length() - 1); // remove last ', '
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private Map<String, String> findDeniedExtensions(FileMetadataContainer arg) {
        Map<String, String> result = new HashMap<>();
        for (FileMetadata file : arg.getFileMetadata()) {
            String name = file.getFile_name();

            int dotIdx = name.lastIndexOf('.');
            if (dotIdx < 0 || name.substring(dotIdx).isBlank()) {
                result.put(file.getId_as_filemetadata(), "<нет расширения>");
                continue;
            }

            String ext = name.substring(dotIdx);
            if (deniedFileExtensionRepository.findByFileExtension(ext).isPresent()) {
                result.put(file.getId_as_filemetadata(), ext);
            }
        }
        return result;
    }
}
